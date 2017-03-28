import java.util.UUID
import java.util.concurrent.Executors

import agni.twitter.util.Future._
import cats.MonadError
import com.datastax.driver.core.{ Cluster, RegularStatement, SimpleStatement, querybuilder }
import com.datastax.driver.core.querybuilder.{ Select, Insert, QueryBuilder => Q }
import com.twitter.util.{ Await, Try, Future => TFuture }
import io.catbird.util._
import shapeless._

object Main extends App {

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

  def newId: UUID = UUID.randomUUID()

  implicit def buildStatement(s: String): RegularStatement = new SimpleStatement(s)

  val remake: Action[Unit] = for {
    _ <- get[Unit](s"""
      |CREATE KEYSPACE IF NOT EXISTS test
      |  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
      |""".stripMargin)
    _ <- get[Unit]("USE test")
    _ <- get[Unit](s"DROP TABLE IF EXISTS user")
    _ <- get[Unit](s"""
      |CREATE TABLE user (
      |  id uuid PRIMARY KEY,
      |  foods set<ascii>,
      |  first_name ascii,
      |  last_name ascii,
      |  age int,
      |  gender ascii,
      |  address map<ascii, ascii>,
      |  baba list<bigint>,
      |  xs list<varint>
      |)""".stripMargin)
  } yield ()

  case class Temp(
    id: UUID,
    foods: Set[String] = Set.empty,
    address: Map[String, String] = Map.empty,
    baba: Vector[Long] = Vector.empty,
    xs: List[BigInt] = List.empty
  )

  val insertUser: Insert = Q.insertInto("user")
    .value("id", Q.bindMarker())
    .value("foods", Q.bindMarker())
    .value("address", Q.bindMarker())
    .value("baba", Q.bindMarker())
    .value("xs", Q.bindMarker())

  val selectUser: Select = Q.select(
    "id", "foods", "first_name", "last_name",
    "age", "gender", "address", "baba", "xs"
  ).from("user")

  val selectUUser: Select = Q.select(
    "id", "foods", "first_name", "last_name",
    "age", "gender", "address", "baba", "xs"
  ).from("user").limit(1)

  val insert = for {
    pstmt1 <- prepare(insertUser)

    id1 <- pure(newId)
    baba1 <- pure(Vector(1L, 2L, Long.MaxValue))
    xs1 <- pure(List(BigInt(10)))
    stmt1 <- bind(pstmt1, id1 :: Set("Banana") :: Map.empty[String, String] :: baba1 :: xs1 :: HNil)
    _ <- get[Unit](stmt1)

    id2 <- pure(newId)
    baba2 <- pure(Vector(-1L, -2L, Long.MinValue))
    stmt2 <- bind(pstmt1, Temp(id = id2, baba = baba2))
    _ <- get[Unit](stmt2)

    id3 <- pure(newId)
    baba3 <- pure(Vector.empty[Long])
    stmt3 <- bind(pstmt1, (id3, Set("Sushi", "Apple"), Map("zip_code" -> "001-0001"), baba3, List.empty[BigInt]))
    _ <- get[Unit](stmt3)
  } yield ()

  val select: Action[List[User]] = get(selectUser)
  val selectOne: Action[Option[User]] = get(selectUUser)

  val action = for {
    _ <- remake
    _ <- insert
    ret <- select
    ret2 <- selectOne
  } yield (ret, ret2)

  val cluster = Cluster.builder()
    .addContactPoint(args(0))
    .withPort(args(1).toInt)
    .build()

  val F = implicitly[MonadError[TFuture, Throwable]]

  Try(cluster.connect()) map { session =>
    val f = F.attempt(action.run(session))
    val r = Await.result(f)
    r fold (
      e => e.printStackTrace(),
      { case (xs, x) => println(x); xs take 100 foreach println }
    )
  } handle {
    case e: Throwable => e.printStackTrace()
  }

  cluster.close()
}
