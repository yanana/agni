import java.util.UUID

import agni._
import cats.MonadError
import cats.data.Xor
import com.datastax.driver.core._
import com.datastax.driver.core.policies._
import com.twitter.util.{ Await, Future }

// import scala.concurrent.Await
// import scala.concurrent.ExecutionContext.Implicits.global
// import scala.concurrent.duration._
import scala.util.Try

object Main extends App {

  object Agni extends Agni[Future, Throwable]
  import Agni._

  import codec._
  // import agni.std.future._
  // import agni.twitter.util.Future._
  import io.catbird.util.futureInstance

  case class User(
    id: UUID,
    foods: Set[String],
    first_name: Option[String],
    last_name: Option[String],
    age: Option[Int],
    gender: Option[String],
    address: Map[String, String]
  )

  implicit val uuidParser: RowDecoder[UUID] = new RowDecoder[UUID] {
    def apply(s: Row, i: Int): UUID = UUID.fromString(s.getString(i))
  }

  def newId: String = UUID.randomUUID().toString

  val remake: String => Action[Unit] = tableName => for {
    _ <- execute[Unit]("CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }")
    _ <- execute[Unit]("USE test")
    _ <- execute[Unit](s"DROP TABLE IF EXISTS $tableName")
    _ <- execute[Unit](s"CREATE TABLE $tableName (id ascii PRIMARY KEY, foods set<ascii>, first_name ascii, last_name ascii, age int, gender ascii, address map<ascii, ascii>)")
  } yield ()

  val batchInsert = for {
    bstmt <- batchOn

    pstmt1 <- prepare("INSERT INTO user (id, foods, address) VALUES (?, {}, {})")

    id1 <- lift(newId)
    _ <- bind(bstmt, pstmt1, id1)

    id2 <- lift(newId)
    _ <- bind(bstmt, pstmt1, id2)

    _ <- execute[Unit](bstmt)

    pstmt2 <- prepare("UPDATE user SET foods = foods + ? WHERE id = ?")

    foods <- lift(Set("Banana"))
    _ <- bind(bstmt, pstmt2, foods, id1)

    pstmt2 <- prepare("UPDATE user SET address = address + ? WHERE id = ?")

    address <- lift(Map("zip_code" -> "001-0001", "country" -> "Japan"))
    _ <- bind(bstmt, pstmt2, address, id2)

    _ <- execute[Unit](bstmt)

  } yield ()

  val selectAll: String => Action[Iterator[User]] = (table) => for {
    ret <- execute[User]("SELECT id, foods, first_name, last_name, age, gender, address FROM user")
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
    .addContactPoint("192.168.99.100")
    .withCodecRegistry(codecRegistry)
    .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
    .withReconnectionPolicy(new ExponentialReconnectionPolicy(500, 5000))
    .withNettyOptions(NettyOptions.DEFAULT_INSTANCE)
    .build()

  val MF = implicitly[MonadError[Future, Throwable]]

  val result = Try(cluster.connect()).map { session =>
    val f = MF.attempt(action.run(session))
    // val result = Await.result(f, Duration.Inf)
    val result = Await.result(f)
    result match {
      case Xor.Right(xs) => xs take 100 foreach println
      case Xor.Left(e) => println(e.getMessage)
    }
  } recover {
    case e: Throwable => e.printStackTrace()
  }

  cluster.close()

}
