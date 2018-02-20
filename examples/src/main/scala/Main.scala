import java.io.Closeable
import java.time.{ LocalDate, ZoneId }
import java.util.{ Date, UUID }
import java.util.concurrent.{ Executor, Executors }

import agni.cache.default._
import agni.free.session._
import agni.twitter.util.Future
import cats.data.Kleisli
import cats.implicits._
import cats.{ MonadError, ~> }
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{ Insert, Select, QueryBuilder => Q }
import com.twitter.util.{ Await, Future => TFuture }
import io.catbird.util._
import org.scalatest.Matchers

import scala.util.control.NonFatal

// Usage: sbt "examples/runMain Main HOST PORT PROTOCOL_VERSION"
// Example: sbt "examples/runMain Main 127.0.0.1 9042 4"
object Main extends App with Matchers {

  implicit val ex: Executor = Executors.newWorkStealingPool

  implicit object F_ extends Future

  case class Author(
    id: UUID,
    first_name: String,
    last_name: String,
    birth: Date,
    gender: String,
    works: Map[String, Int]
  )

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
      "Mother Ireland" -> 1976
    )),
    Author(UUID.randomUUID(), "Benedict", "Kiely", (1919, 8, 15), "male", Map(
      "The Collected Stories of Benedict Kiely" -> 2001,
      "The Trout in the Turnhole" -> 1996,
      "A Letter to Peachtree" -> 1987,
      "The State of Ireland: A Novella and Seventeen Short Stories" -> 1981,
      "A Cow in the House" -> 1978,
      "A Ball of Malt and Madame Butterfly" -> 1973,
      "A Journey to the Seven Streams" -> 1963
    )),
    Author(UUID.randomUUID(), "Darren", "Shan", (1972, 7, 2), "male", Map(
      "Cirque Du Freak" -> 2000,
      "The Vampire's Assistant" -> 2000,
      "Tunnels of Blood" -> 2000
    ))
  )

  implicit def buildStatement(s: String): RegularStatement = new SimpleStatement(s)

  def getCluster = TFuture(Cluster.builder()
    .addContactPoint(args(0))
    .withPort(args(1).toInt)
    .withProtocolVersion(ProtocolVersion.fromInt(args(2).toInt))
    .build())

  def bracket[R <: Closeable, F[_], A](fr: F[R])(f: R => F[A])(implicit F: MonadError[F, Throwable]): F[A] =
    F.flatMap(fr)(r => {
      F.recoverWith(F.flatMap(f(r)) { a => r.close(); F.pure(a) }) {
        case NonFatal(e) =>
          try r.close() catch {
            case NonFatal(e) => e.printStackTrace()
          }
          F.raiseError(e)
      }
    })

  val S = implicitly[SessionOps[SessionOp]]

  import Query._

  def remake: S.SessionF[Unit] = for {
    _ <- S.prepare(createKeyspace).flatMap(p => S.execute[Unit](p.bind()))
    _ <- S.prepare(useKeyspace).flatMap(p => S.execute[Unit](p.bind()))
    _ <- S.prepare(useKeyspace).flatMap(p => S.execute[Unit](p.bind()))
    _ <- S.prepare(dropTable).flatMap(p => S.execute[Unit](p.bind()))
    _ <- S.prepare(createTable).flatMap(p => S.execute[Unit](p.bind()))
  } yield ()

  def insertUser(p: PreparedStatement, a: Author): S.SessionF[Unit] =
    S.bind(p, a).flatMap(b => S.execute[Unit](b))

  def action: S.SessionF[List[Author]] = for {
    _ <- remake
    _ <- S.prepare(insertUserQuery).flatMap(p => users.traverse(x => insertUser(p, x)))
    xs <- S.prepare(selectUserQuery).flatMap(p => S.execute[List[Author]](p.bind()))
  } yield xs

  type H[A] = Kleisli[TFuture, Session, A]

  def interpret(implicit handler: SessionOp ~> H): H[List[Author]] =
    action.foldMap(handler)

  def run(): TFuture[List[Author]] = {
    import SessionOp.Handler._
    bracket(getCluster)(c => bracket(TFuture(c.connect()))(interpret.run))
  }

  implicit val authorOrdering: Ordering[Author] = new Ordering[Author] {
    def compare(x: Author, y: Author): Int = y.id.compareTo(x.id)
  }

  Await.result(run().attempt) match {
    case Left(e) => throw e
    case Right(xs) =>
      println("Result:")
      xs foreach println
      assert(users.sorted === xs.sorted)
  }
}

object Query {
  val createKeyspace =
    s"""CREATE KEYSPACE IF NOT EXISTS agni_test
       |  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
       |""".stripMargin

  val useKeyspace =
    "USE agni_test"

  val dropTable =
    s"DROP TABLE IF EXISTS author"

  val createTable =
    s"""CREATE TABLE author (
       |  id uuid PRIMARY KEY,
       |  first_name ascii,
       |  last_name ascii,
       |  birth timestamp,
       |  gender ascii,
       |  works map<ascii, int>,
       |)""".stripMargin

  val insertUserQuery: Insert =
    Q.insertInto("author")
      .value("id", Q.bindMarker())
      .value("first_name", Q.bindMarker())
      .value("last_name", Q.bindMarker())
      .value("birth", Q.bindMarker())
      .value("gender", Q.bindMarker())
      .value("works", Q.bindMarker())

  val selectUserQuery: Select =
    Q.select.all.from("author") // .orderBy(Q.asc("birth"))
}

