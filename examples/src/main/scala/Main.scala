import java.io.Closeable
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.{ Executor, Executors }

import agni.free.session._
import agni.effect.Task
import cats.data.Kleisli
import cats.implicits._
import cats.effect.{ IO, IOApp }
import cats.{ MonadError, ~> }
import com.datastax.oss.driver.api.core.cql.{ PreparedStatement, SimpleStatement }
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.querybuilder.{ QueryBuilder => Q }
import org.scalatest.Matchers

import scala.util.control.NonFatal
import cats.effect.ExitCode

// Usage: sbt "examples/runMain Main HOST PORT PROTOCOL_VERSION"
// Example: sbt "examples/runMain Main 127.0.0.1 9042 4"
object Main extends IOApp with Matchers {

  implicit val ex: Executor = Executors.newWorkStealingPool

  implicit object F_ extends Task[IO]

  case class Author(
    id: UUID,
    first_name: String,
    last_name: String,
    birth: LocalDate,
    gender: String,
    works: Map[String, Int]
  )

  val users = List(
    Author(UUID.randomUUID(), "Edna", "O'Brien", LocalDate.of(1932, 12, 15), "female", Map(
      "The Country Girls" -> 1960,
      "Girl with Green Eyes" -> 1962,
      "Girls in Their Married Bliss" -> 1964,
      "August is a Wicked Month" -> 1965,
      "Casualties of Peace" -> 1966,
      "Mother Ireland" -> 1976
    )),
    Author(UUID.randomUUID(), "Benedict", "Kiely", LocalDate.of(1919, 8, 15), "male", Map(
      "The Collected Stories of Benedict Kiely" -> 2001,
      "The Trout in the Turnhole" -> 1996,
      "A Letter to Peachtree" -> 1987,
      "The State of Ireland: A Novella and Seventeen Short Stories" -> 1981,
      "A Cow in the House" -> 1978,
      "A Ball of Malt and Madame Butterfly" -> 1973,
      "A Journey to the Seven Streams" -> 1963
    )),
    Author(UUID.randomUUID(), "Darren", "Shan", LocalDate.of(1972, 7, 2), "male", Map(
      "Cirque Du Freak" -> 2000,
      "The Vampire's Assistant" -> 2000,
      "Tunnels of Blood" -> 2000
    ))
  )

  implicit def buildStatement(s: String): SimpleStatement = SimpleStatement.newInstance(s)

  def newSession = IO(CqlSession.builder().build())

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

  type H[A] = Kleisli[IO, CqlSession, A]

  def interpret(implicit handler: SessionOp ~> H): H[List[Author]] =
    action.foldMap(handler)

  def run(args: List[String]): IO[ExitCode] = {
    import SessionOp.Handler._
    newSession.bracket(interpret.run)(c => IO(c.close()))
      .flatTap(xs => IO(xs.foreach(println)))
      .flatMap(xs => IO(assert(users.sorted === xs.sorted)))
      .map(_ => ExitCode.Success)
  }

  implicit val authorOrdering: Ordering[Author] = new Ordering[Author] {
    def compare(x: Author, y: Author): Int = y.id.compareTo(x.id)
  }

  // Await.result(run().attempt) match {
}

object Query {
  private[this] val keyspace = "agni_test"
  private[this] val tableName = "author"

  val createKeyspace =
    s"""CREATE KEYSPACE IF NOT EXISTS $keyspace
       |  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
       |""".stripMargin

  val dropTable =
    s"DROP TABLE IF EXISTS $keyspace.$tableName"

  val createTable =
    s"""CREATE TABLE $keyspace.$tableName (
       |  id uuid PRIMARY KEY,
       |  first_name ascii,
       |  last_name ascii,
       |  birth date,
       |  gender ascii,
       |  works map<ascii, int>,
       |)""".stripMargin

  val insertUserQuery =
    Q.insertInto(keyspace, tableName)
      .value("id", Q.bindMarker())
      .value("first_name", Q.bindMarker())
      .value("last_name", Q.bindMarker())
      .value("birth", Q.bindMarker())
      .value("gender", Q.bindMarker())
      .value("works", Q.bindMarker())
      .build()

  val selectUserQuery =
    Q.selectFrom(keyspace, tableName).all().build()
}
