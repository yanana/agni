import java.net.InetAddress
import java.util.UUID
import java.util.concurrent.ForkJoinPool

import agni._
import cats.MonadError
import cats.std.future._
import cats.data.Xor
import com.datastax.driver.core.policies.{ DefaultRetryPolicy, ExponentialReconnectionPolicy }
import com.datastax.driver.core.{ NettyOptions, Cluster, Row }

import scala.concurrent.{ Future, ExecutionContext, Await }
import scala.concurrent.duration._

object Main extends App {

  implicit val executionContext = ExecutionContext.fromExecutorService(new ForkJoinPool())

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

  import Agni._

  def newId: String = UUID.randomUUID().toString

  val remake: String => Action[Future, Unit] = tableName => for {
    _ <- execute[Unit](s"DROP TABLE IF EXISTS $tableName")
    _ <- execute[Unit](s"""
           |CREATE TABLE $tableName (
           |  id ascii PRIMARY KEY,
           |  foods set<ascii>,
           |  first_name ascii,
           |  last_name ascii,
           |  age int,
           |  gender ascii,
           |  address map<ascii, ascii>
           |)""".stripMargin)
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

  val selectAll: String => Action[Future, Iterator[User]] = (table) => for {
    ret <- execute[User]("SELECT id, foods, first_name, last_name, age, gender, address FROM user")
  } yield ret

  val action = for {
    _ <- remake("user")
    _ <- batchInsert
    ret <- selectAll("user")
  } yield ret

  val cluster = Cluster.builder()
    .addContactPoints(InetAddress.getByName("192.168.99.100"))
    .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
    .withReconnectionPolicy(new ExponentialReconnectionPolicy(500, 5000))
    .withNettyOptions(NettyOptions.DEFAULT_INSTANCE)
    .build()

  try {
    val MF = implicitly[MonadError[Future, Throwable]]

    val session = cluster.connect("test")
    val f = MF.attempt(action.run(session))
    val result = Await.result(f, Duration.Inf)
    result match {
      case Xor.Right(xs) => xs take 100 foreach println
      case Xor.Left(e) => println(e.getMessage)
    }
  } catch {
    case e: Throwable => e.printStackTrace()
  }

  cluster.close()

}
