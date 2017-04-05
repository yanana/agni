import java.util.UUID
import java.util.concurrent.Executors

import agni.twitter.util.Future
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{ Insert, Select, QueryBuilder => Q }
import com.twitter.util.{ Await, Try }
import io.catbird.util._
import org.scalatest.Matchers

import agni.cache.default._
object F extends Future

// Usage: sbt "examples/runMain Main 127.0.0.1 9042"
object Main extends App with Matchers {

  implicit val executor = Executors.newWorkStealingPool()

  case class User(
    id: UUID,
    first_name: String,
    last_name: String,
    birth: LocalDate,
    gender: String,
    works: List[String]
  )

  implicit def tuple3to(a: (Int, Int, Int)): LocalDate = (LocalDate.fromYearMonthDay _).tupled(a)

  val users = List(
    User(UUID.randomUUID(), "Edna", "O'Brien", (1932, 12, 15), "female", List("The Country Girls", "Girl with Green Eyes", "Girls in Their Married Bliss", "August is a Wicked Month", "Casualties of Peace", "Mother Ireland")),
    User(UUID.randomUUID(), "Benedict", "Kiely", (1919, 8, 15), "male", List("The Collected Stories of Benedict Kiely", "The Trout in the Turnhole", "A Letter to Peachtree", "The State of Ireland: A Novella and Seventeen Short Stories", "A Cow in the House", "A Ball of Malt and Madame Butterfly", "A Journey to the Seven Streams")),
    User(UUID.randomUUID(), "Darren", "Shan", (1972, 7, 2), "male", List("Cirque Du Freak", "The Vampire's Assistant", "Tunnels of Blood"))
  )

  implicit def buildStatement(s: String): RegularStatement = new SimpleStatement(s)

  val remake: F.Action[Unit] =
    F.get[Unit](s"""CREATE KEYSPACE IF NOT EXISTS agni_test
                   |  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
                   |""".stripMargin) >>
      F.get[Unit]("USE agni_test") >>
      F.get[Unit](s"DROP TABLE IF EXISTS user") >>
      F.get[Unit](s"""CREATE TABLE user (
                   |  id uuid PRIMARY KEY,
                   |  first_name text,
                   |  last_name text,
                   |  birth date,
                   |  gender ascii,
                   |  works list<text>,
                   |)""".stripMargin)

  val insertUserQuery: Insert =
    Q.insertInto("user")
      .value("id", Q.bindMarker())
      .value("first_name", Q.bindMarker())
      .value("last_name", Q.bindMarker())
      .value("birth", Q.bindMarker())
      .value("gender", Q.bindMarker())
      .value("works", Q.bindMarker())

  val selectUserQuery: Select =
    Q.select.all.from("user")

  def insertUser(p: PreparedStatement, a: User): F.Action[Unit] =
    F.bind(p, a) >>= (b => F.getAsync[Unit](b))

  val action: F.Action[List[User]] =
    remake >>
      (F.prepare(insertUserQuery) >>= (p => users.traverse(insertUser(p, _)))) >>
      F.get[List[User]](selectUserQuery)

  val cluster = Cluster.builder()
    .addContactPoint(args(0))
    .withPort(args(1).toInt)
    .build()

  implicit val uuidOrd: Ordering[User] = (x: User, y: User) => x.id.compareTo(y.id)

  Try(cluster.connect()) map { session =>
    val Right(xs) = Await.result(action.run(session).attempt)
    assert(users.sorted === xs.sorted)
    xs foreach println
  } handle {
    case e: Throwable =>
      e.printStackTrace()
      sys.exit(1)
  } ensure cluster.close()
}
