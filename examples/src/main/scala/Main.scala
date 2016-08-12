import java.util.UUID
import java.util.concurrent.Executors

import agni._
import agni.twitter.util.Future
import cats.MonadError
import cats.data.Xor
import cats.std.list._
import cats.syntax.traverse._
import com.datastax.driver.core._
import com.datastax.driver.core.policies._
import com.twitter.util.{ Await, Try, Future => TFuture }
import shapeless._

object Main extends App {

  import io.catbird.util._
  import Future._

  import codec._

  implicit val executor = Executors.newWorkStealingPool()

  case class User(
    id: UUID,
    foods: Set[String],
    first_name: Option[String],
    last_name: Option[String],
    age: Option[Int],
    gender: Option[String],
    address: Map[String, String],
    baba: Option[Vector[Long]]
  )

  implicit val uuidParser: RowDecoder[UUID] = new RowDecoder[UUID] {
    def apply(s: Row, i: Int): UUID = UUID.fromString(s.getString(i))
  }

  def newId: String = UUID.randomUUID().toString

  val remake: String => Action[Unit] = tableName => for {
    _ <- execute[Unit]("CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }")
    _ <- execute[Unit]("USE test")
    _ <- execute[Unit](s"DROP TABLE IF EXISTS $tableName")
    _ <- execute[Unit](s"""
      |CREATE TABLE $tableName (
      |  id ascii PRIMARY KEY,
      |  foods set<ascii>,
      |  first_name ascii,
      |  last_name ascii,
      |  age int,
      |  gender ascii,
      |  address map<ascii, ascii>,
      |  baba list<bigint>
      |)""".stripMargin)
  } yield ()

  val none: Option[Vector[Long]] = None

  val batchInsert = for {
    pstmt1 <- prepare("INSERT INTO user (id, foods, address, baba) VALUES (?, {}, {}, ?)")

    id1 <- lift(newId)
    baba1 <- lift(Vector(1L, 2L, Long.MaxValue))
    stmt1 <- bind(pstmt1, id1 :: baba1 :: HNil)

    id2 <- lift(newId)
    baba2 <- lift(Vector(-1L, -2L, Long.MinValue))
    stmt2 <- bind(pstmt1, id2 :: none :: HNil)

    pstmt2 <- prepare("UPDATE user SET foods = foods + ? WHERE id = ?")

    foods <- lift(Set("Banana"))
    stmt2 <- bind(pstmt2, foods :: id1 :: HNil)

    pstmt2 <- prepare("UPDATE user SET address = address + ? WHERE id = ?")

    address <- lift(Map("zip_code" -> "001-0001", "country" -> "Japan"))
    stmt3 <- bind(pstmt2, address :: id2 :: HNil)

    stmts <- lift(stmt1 :: stmt2 :: stmt3 :: Nil)
    _ <- stmts.traverse(executeAsync[Unit])

  } yield ()

  val selectAll: String => Action[Iterator[User]] = (table) => for {
    ret <- execute[User]("SELECT id, foods, first_name, last_name, age, gender, address, baba FROM user")
  } yield ret

  val action = for {
    _ <- remake("user")
    _ <- batchInsert
    ret <- selectAll("user")
  } yield ret

  val codecRegistry = CodecRegistry.DEFAULT_INSTANCE
  codecRegistry.register(new LongToObjectCodec)
  codecRegistry.register(new AsciiToObjectCodec)

  val cluster = Cluster.builder()
    .addContactPoint(args(0))
    .withPort(args(1).toInt)
    .withCodecRegistry(codecRegistry)
    .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
    .withReconnectionPolicy(new ExponentialReconnectionPolicy(500, 5000))
    .withNettyOptions(NettyOptions.DEFAULT_INSTANCE)
    .build()

  val F = implicitly[MonadError[TFuture, Throwable]]

  Try(cluster.connect()) map { session =>
    val f = F.attempt(action.run(session))
    val result = Await.result(f)
    result match {
      case Xor.Right(xs) => xs take 100 foreach println
      case Xor.Left(e) => e.printStackTrace()
    }
  } handle {
    case e: Throwable => e.printStackTrace()
  }

  cluster.close()

}
