import java.time.{ LocalDate, ZoneId }
import java.util.{ Date, UUID }
import java.util.concurrent.{ Executor, Executors }

import agni.cache.default._
import agni.twitter.util.Future
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{ Insert, Select, QueryBuilder => Q }
import com.twitter.util.{ Await, Try }
import com.twitter.util.{ Future => TFuture }
import io.catbird.util._
import org.scalatest.Matchers

// Usage: sbt "examples/runMain Main HOST PORT PROTOCOL_VERSION"
// Example: sbt "examples/runMain Main 127.0.0.1 9042 4"
object Main extends App with Matchers {

  implicit val ex: Executor = Executors.newWorkStealingPool

  object F extends Future

  case class Author(
    id: UUID,
    first_name: String,
    last_name: String,
    birth: Date,
    gender: String,
    works: Map[String, Int])

  implicit def tuple3to(a: (Int, Int, Int)): Date = {
    val local = LocalDate.of(a._1, a._2, a._3)
    Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant)
  }

  val users = List(
    Author(UUID.randomUUID(), "Edna", "O'Brien", (1932, 12, 15), "female", Map(
      "The Country Girls" -> 1960,
      "Girl with Green Eyes" -> 1962,
      "Girls in Their Married Bliss" -> 1964,
      "August is a Wicked Month" -> 1965,
      "Casualties of Peace" -> 1966,
      "Mother Ireland" -> 1976)),
    Author(UUID.randomUUID(), "Benedict", "Kiely", (1919, 8, 15), "male", Map(
      "The Collected Stories of Benedict Kiely" -> 2001,
      "The Trout in the Turnhole" -> 1996,
      "A Letter to Peachtree" -> 1987,
      "The State of Ireland: A Novella and Seventeen Short Stories" -> 1981,
      "A Cow in the House" -> 1978,
      "A Ball of Malt and Madame Butterfly" -> 1973,
      "A Journey to the Seven Streams" -> 1963)),
    Author(UUID.randomUUID(), "Darren", "Shan", (1972, 7, 2), "male", Map(
      "Cirque Du Freak" -> 2000,
      "The Vampire's Assistant" -> 2000,
      "Tunnels of Blood" -> 2000)))

  implicit def buildStatement(s: String): RegularStatement = new SimpleStatement(s)

  val remake: TFuture[Unit] =
    F.get[Unit](s"""CREATE KEYSPACE IF NOT EXISTS agni_test
                   |  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
                   |""".stripMargin) >>
      F.get[Unit]("USE agni_test") >>
      F.get[Unit](s"DROP TABLE IF EXISTS author") >>
      F.get[Unit](s"""CREATE TABLE author (
                   |  id uuid PRIMARY KEY,
                   |  first_name ascii,
                   |  last_name ascii,
                   |  birth timestamp,
                   |  gender ascii,
                   |  works map<ascii, int>,
                   |)""".stripMargin)

  val insertUserQuery: Insert =
    Q.insertInto("author")
      .value("id", Q.bindMarker())
      .value("first_name", Q.bindMarker())
      .value("last_name", Q.bindMarker())
      .value("birth", Q.bindMarker())
      .value("gender", Q.bindMarker())
      .value("works", Q.bindMarker())

  val selectUserQuery: Select =
    Q.select.all.from("author")

  def insertUser(p: PreparedStatement, a: Author): TFuture[Unit] =
    F.bind(p, a) >>= (b => F.get[Unit](b))

  val action: TFuture[List[Author]] =
    remake >>
      (F.prepare(insertUserQuery) >>=
        (p => users.traverse(insertUser(p, _)))) >>
        F.get[List[Author]](selectUserQuery)

  val cluster = Cluster.builder()
    .addContactPoint(args(0))
    .withPort(args(1).toInt)
    .withProtocolVersion(ProtocolVersion.fromInt(args(2).toInt))
    .build()

  implicit val authorOrdering: Ordering[Author] =
    (x: Author, y: Author) => x.id.compareTo(y.id)

  Try(cluster.connect()) map { session =>
    Await.result(action.attempt) match {
      case Left(e) => throw e
      case Right(xs) =>
        assert(users.sorted === xs.sorted)
        xs foreach println
    }
  } handle {
    case e: Throwable =>
      e.printStackTrace()
      sys.exit(1)
  } ensure cluster.close()
}
