package benchmarks.io

import java.util.UUID
import java.util.concurrent.{ Executor, Executors, TimeUnit }

import agni.{ Binder, Result, RowDecoder }
import cats.instances.try_._
import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._
import cats.syntax.cartesian._
import com.datastax.driver.core.querybuilder.{ Insert, Select, QueryBuilder => Q }
import com.datastax.driver.core._
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.guava.CaffeinatedGuava
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures, MoreExecutors }
import org.openjdk.jmh.annotations._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
abstract class CassandraClientBenchmark {

  implicit val context: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  implicit val cache: Cache[String, PreparedStatement] =
    CaffeinatedGuava.build(Caffeine.newBuilder())

  object SF extends agni.std.Future
  object TF extends agni.twitter.util.Future
  object ST extends agni.std.Try
  object MF extends agni.monix.Task

  case class User(
    id: UUID = UUID.randomUUID(),
    first_name: String = "",
    last_name: String = "",
    gender: String = "",
    works: List[String] = List.empty
  )

  type UserTuple = (UUID, String, String, String, List[String])

  val uuid = UUID.randomUUID()
  val uuid2 = UUID.randomUUID()
  val uuid3 = UUID.randomUUID()

  val users = User(uuid, "Edna", "O'Brien", "female", List("The Country Girls", "Girl with Green Eyes", "Girls in Their Married Bliss", "August is a Wicked Month", "Casualties of Peace", "Mother Ireland")) ::
    User(uuid2, "Benedict", "Kiely", "male", List("The Collected Stories of Benedict Kiely", "The Trout in the Turnhole", "A Letter to Peachtree", "The State of Ireland: A Novella and Seventeen Short Stories", "A Cow in the House", "A Ball of Malt and Madame Butterfly", "A Journey to the Seven Streams")) ::
    User(uuid3, "Darren", "Shan", "male", List("Cirque Du Freak", "The Vampire's Assistant", "Tunnels of Blood")) ::
    (1 to 100).map(_ =>
      User(UUID.randomUUID(), "Darren", "Shan", "male", List("Cirque Du Freak", "The Vampire's Assistant", "Tunnels of Blood"))).toList

  implicit def buildStatement(s: String): RegularStatement = new SimpleStatement(s)

  val keyspace = "agni_bench"

  val remake: ST.Action[Unit] = for {
    _ <- ST.get[Unit](s"""CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }""".stripMargin)
    _ <- ST.get[Unit](s"USE $keyspace")
    _ <- ST.get[Unit](s"DROP TABLE IF EXISTS user")
    _ <- ST.get[Unit](s"""CREATE TABLE user (id uuid PRIMARY KEY, first_name text, last_name text, gender ascii, works list<text>)""".stripMargin)
  } yield ()

  val insertUserQuery: Insert = Q.insertInto("user")
    .value("id", Q.bindMarker())
    .value("first_name", Q.bindMarker())
    .value("last_name", Q.bindMarker())
    .value("gender", Q.bindMarker())
    .value("works", Q.bindMarker())

  def insertUser(p: PreparedStatement, a: User): ST.Action[Unit] = for {
    b <- ST.bind(p, a)
    _ <- ST.get[Unit](b)
  } yield ()

  val insert: ST.Action[Unit] = for {
    pstmt <- ST.prepare(insertUserQuery)
    _ <- users.traverse(insertUser(pstmt.setIdempotent(true).setConsistencyLevel(ConsistencyLevel.ALL), _))
  } yield ()

  val action: ST.Action[Unit] = for {
    _ <- remake
    _ <- insert
  } yield ()

  var cluster: Cluster = _
  var session: Session = _

  @Setup()
  def setup(): Unit = {
    cluster = Cluster.builder()
      .addContactPoint(sys.env.getOrElse("CASSANDRA_HOST", "localhost"))
      .withPort(sys.env.getOrElse("CASSANDRA_PORT", "9042").toInt)
      .withProtocolVersion(ProtocolVersion.V3)
      .build()
    action.run(cluster.connect()).get
    session = cluster.connect(keyspace)
  }

  @TearDown
  def tearDown(): Unit = {
    ST.clear()
    SF.clear()
    ST.clear()
    cluster.close()
  }
}

class AgniBenchmark extends CassandraClientBenchmark {

  val selectUsers: Select = Q.select("id", "first_name", "last_name", "gender", "works").from("user") // .limit(3)

  @Benchmark
  def javaStream: java.util.stream.Stream[Row] = {
    val f: ST.Action[java.util.stream.Stream[Row]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[java.util.stream.Stream[Row]](b)
    } yield l
    f.run(session).get
  }

  @Benchmark
  def iterToList: List[User] = {
    import agni.syntax._
    val f: ST.Action[List[User]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[Iterator[Row]](b)
    } yield {
      l.map { row =>
        val user: Result[User] = row.decode(RowDecoder.apply[User])
        user.fold(throw _, identity)
      }.toList
    }
    f.run(session).get
  }

  @Benchmark
  def list: List[User] = {
    val f: ST.Action[List[User]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[List[User]](b)
    } yield l
    f.run(session).get
  }

  @Benchmark
  def vector: Vector[User] = {
    val f: ST.Action[Vector[User]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[Vector[User]](b)
    } yield l
    f.run(session).get
  }

  @Benchmark
  def listTuple: List[UserTuple] = {
    val f: ST.Action[List[UserTuple]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[List[UserTuple]](b)
    } yield l
    f.run(session).get
  }

  @Benchmark
  def vectorTuple: Vector[UserTuple] = {
    val f: ST.Action[Vector[UserTuple]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[Vector[UserTuple]](b)
    } yield l
    f.run(session).get
  }

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

  implicit val bindUUID: Binder[UUID] = (pstmt: PreparedStatement, a: UUID) => pstmt.bind(a)

  @Benchmark
  def one: Option[User] = {
    val f: ST.Action[Option[User]] = for {
      p <- ST.prepare(selectUser)
      b <- ST.bind(p, uuid)
      l <- ST.get[Option[User]](b)
    } yield l
    f.run(session).get
  }

  @Benchmark
  def oneTuple: Option[UserTuple] = {
    val f: ST.Action[Option[UserTuple]] = for {
      p <- ST.prepare(selectUser)
      b <- ST.bind(p, uuid)
      l <- ST.get[Option[UserTuple]](b)
    } yield l
    f.run(session).get
  }
}

class AgniSyncBenchmark extends CassandraClientBenchmark {

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

  implicit val bindUUID: Binder[UUID] = (pstmt: PreparedStatement, a: UUID) => pstmt.bind(a)

  @Benchmark
  def one: Option[User] = {
    val f: ST.Action[Option[User]] = for {
      b <- ST.prepare(selectUser).andThen(ST.bind(uuid))
      l <- ST.get[Option[User]](b)
    } yield l
    f.run(session).get
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa: ST.Action[Option[User]] = for {
      b <- ST.prepare(selectUser).andThen(ST.bind(uuid))
      l <- ST.get[Option[User]](b)
    } yield l

    val fb: ST.Action[Option[User]] = for {
      b <- ST.prepare(selectUser).andThen(ST.bind(uuid2))
      l <- ST.get[Option[User]](b)
    } yield l

    val fc: ST.Action[Option[User]] = for {
      b <- ST.prepare(selectUser).andThen(ST.bind(uuid3))
      l <- ST.get[Option[User]](b)
    } yield l

    val f = (fa |@| fb |@| fc).tupled

    f.run(session).get
  }
}

class AgniAsyncBenchmark extends CassandraClientBenchmark {

  implicit val executor: Executor = MoreExecutors.directExecutor()

  implicit val bindUUID: Binder[UUID] = (pstmt: PreparedStatement, a: UUID) => pstmt.bind(a)

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

  @Benchmark
  def one: Option[User] = {
    val f: SF.Action[Option[User]] = for {
      b <- SF.prepare(selectUser).andThen(SF.bind(uuid))
      l <- SF.getAsync[Option[User]](b)
    } yield l
    Await.result(f.run(session), Duration.Inf)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa: SF.Action[Option[User]] = for {
      b <- SF.prepare(selectUser).andThen(SF.bind(uuid))
      l <- SF.getAsync[Option[User]](b)
    } yield l

    val fb: SF.Action[Option[User]] = for {
      b <- SF.prepare(selectUser).andThen(SF.bind(uuid2))
      l <- SF.getAsync[Option[User]](b)
    } yield l

    val fc: SF.Action[Option[User]] = for {
      b <- SF.prepare(selectUser).andThen(SF.bind(uuid3))
      l <- SF.getAsync[Option[User]](b)
    } yield l

    val f = (fa |@| fb |@| fc).tupled

    Await.result(f.run(session), Duration.Inf)
  }
}

class AgniTwitterAsyncBenchmark extends CassandraClientBenchmark {
  import com.twitter.util.{ Await => TAwait }
  import io.catbird.util.twitterFutureInstance

  implicit val executor: Executor = MoreExecutors.directExecutor()

  implicit val bindUUID: Binder[UUID] = (pstmt: PreparedStatement, a: UUID) => pstmt.bind(a)

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

  @Benchmark
  def one: Option[User] = {
    val f: TF.Action[Option[User]] = for {
      b <- TF.prepare(selectUser).andThen(TF.bind(uuid))
      l <- TF.getAsync[Option[User]](b)
    } yield l
    TAwait.result(f.run(session))
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa: TF.Action[Option[User]] = for {
      b <- TF.prepare(selectUser).andThen(TF.bind(uuid))
      l <- TF.getAsync[Option[User]](b)
    } yield l

    val fb: TF.Action[Option[User]] = for {
      b <- TF.prepare(selectUser).andThen(TF.bind(uuid2))
      l <- TF.getAsync[Option[User]](b)
    } yield l

    val fc: TF.Action[Option[User]] = for {
      b <- TF.prepare(selectUser).andThen(TF.bind(uuid3))
      l <- TF.getAsync[Option[User]](b)
    } yield l

    val f = (fa |@| fb |@| fc).tupled

    TAwait.result(f.run(session))
  }
}

class AgniMonixAsyncBenchmark extends CassandraClientBenchmark {
  import agni.monix.cats._
  import monix.execution.Scheduler
  import scala.concurrent.duration._

  implicit val executor: Executor = MoreExecutors.directExecutor()
  implicit val sche: Scheduler = Scheduler.apply(context)

  implicit val bindUUID: Binder[UUID] = (pstmt: PreparedStatement, a: UUID) => pstmt.bind(a)

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

  @Benchmark
  def one: Option[User] = {
    val f: MF.Action[Option[User]] = for {
      b <- MF.prepare(selectUser).andThen(MF.bind(uuid))
      l <- MF.getAsync[Option[User]](b)
    } yield l
    Await.result(f.run(session).runAsync, 10 seconds)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa: MF.Action[Option[User]] = for {
      b <- MF.prepare(selectUser).andThen(MF.bind(uuid))
      l <- MF.getAsync[Option[User]](b)
    } yield l

    val fb: MF.Action[Option[User]] = for {
      b <- MF.prepare(selectUser).andThen(MF.bind(uuid2))
      l <- MF.getAsync[Option[User]](b)
    } yield l

    val fc: MF.Action[Option[User]] = for {
      b <- MF.prepare(selectUser).andThen(MF.bind(uuid3))
      l <- MF.getAsync[Option[User]](b)
    } yield l

    val f = (fa |@| fb |@| fc).tupled

    Await.result(f.run(session).runAsync, 10 seconds)
  }
}

class CasJavaDriverAsyncBenchmark extends CassandraClientBenchmark {

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

  def getAsync(stmt: Statement): Future[Option[User]] = {
    val pp = Promise[Option[User]]
    Futures.addCallback(session.executeAsync(stmt), new FutureCallback[ResultSet] {
      import scala.collection.JavaConverters._
      override def onFailure(t: Throwable): Unit = pp.failure(t)
      override def onSuccess(result: ResultSet): Unit = {
        pp.success(Option(result.one()).map { x =>
          User(
            id = x.getUUID("id"),
            first_name = x.getString("first_name"),
            last_name = x.getString("last_name"),
            gender = x.getString("gender"),
            works = x.getList[String]("works", classOf[String]).asScala.toList
          )
        })
      }
    })
    pp.future
  }

  @Benchmark
  def one: Option[User] = {
    val p = cache.get(selectUser.toString, () => session.prepare(selectUser))
    Await.result(getAsync(p.bind(uuid)), Duration.Inf)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val p = cache.get(selectUser.toString, () => session.prepare(selectUser))
    val fa = getAsync(p.bind(uuid))
    val fb = getAsync(p.bind(uuid2))
    val fc = getAsync(p.bind(uuid3))

    val f = (fa |@| fb |@| fc).tupled

    Await.result(f, Duration.Inf)
  }
}
