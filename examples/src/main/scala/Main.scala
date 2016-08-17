import java.util.UUID
import java.util.concurrent.Executors

import agni._
import agni.twitter.util.Future._
import cats.MonadError
import cats.data.Xor
import com.datastax.driver.core._, querybuilder.{ QueryBuilder => Q, _ }, policies._
import com.twitter.util.{ Await, Try, Future => TFuture }
import io.catbird.util._
import shapeless._

object Main extends App {
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

  def newId = UUID.randomUUID()

  val remake: Action[Unit] = for {
    _ <- execute[Unit](s"""
      |CREATE KEYSPACE IF NOT EXISTS test
      |  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
      |""".stripMargin)
    _ <- execute[Unit]("USE test")
    _ <- execute[Unit](s"DROP TABLE IF EXISTS user")
    _ <- execute[Unit](s"""
      |CREATE TABLE user (
      |  id uuid PRIMARY KEY,
      |  foods set<ascii>,
      |  first_name ascii,
      |  last_name ascii,
      |  age int,
      |  gender ascii,
      |  address map<ascii, ascii>,
      |  baba list<bigint>
      |)""".stripMargin)
  } yield ()

  case class Temp(
    id: UUID,
    foods: Set[String] = Set.empty,
    address: Map[String, String] = Map.empty,
    baba: Vector[Long] = Vector.empty
  )

  val insertUser = Q.insertInto("user")
    .value("id", Q.bindMarker())
    .value("foods", Q.bindMarker())
    .value("address", Q.bindMarker())
    .value("baba", Q.bindMarker())

  val selectUser = Q.select("id", "foods", "first_name", "last_name", "age", "gender", "address", "baba").from("user")

  val batchInsert = for {
    pstmt1 <- prepare(insertUser.toString)

    id1 <- lift(newId)
    baba1 <- lift(Vector(1L, 2L, Long.MaxValue))
    stmt1 <- bind(pstmt1, id1 :: Set("Banana") :: Map.empty[String, String] :: baba1 :: HNil)
    _ <- execute[Unit](stmt1)

    id2 <- lift(newId)
    baba2 <- lift(Vector(-1L, -2L, Long.MinValue))
    stmt2 <- bind(pstmt1, Temp(id = id2, baba = baba2))
    _ <- execute[Unit](stmt2)

    id3 <- lift(newId)
    baba3 <- lift(Vector.empty[Long])
    stmt3 <- bind(pstmt1, (id3, Set("Sushi", "Apple"), Map("zip_code" -> "001-0001"), baba3))
    _ <- execute[Unit](stmt2)
  } yield ()

  val selectAll: Action[Iterator[User]] = for {
    ret <- execute[User](selectUser.toString)
  } yield ret

  val action = for {
    _ <- remake
    _ <- batchInsert
    ret <- selectAll
  } yield ret

  val codecRegistry = CodecRegistry.DEFAULT_INSTANCE
  codecRegistry.register(new LongToObjectCodec)
  codecRegistry.register(new AsciiToObjectCodec)

  val cluster = Cluster.builder()
    .addContactPoint(args(0))
    .withPort(args(1).toInt)
    .withCodecRegistry(codecRegistry)
    .build()

  val F = implicitly[MonadError[TFuture, Throwable]]

  Try(cluster.connect()) map { session =>
    val f = F.attempt(action.run(session))
    val result = Await.result(f)
    result fold (
      e => e.printStackTrace(),
      xs => xs take 100 foreach println
    )
  } handle {
    case e: Throwable => e.printStackTrace()
  }

  cluster.close()
}
