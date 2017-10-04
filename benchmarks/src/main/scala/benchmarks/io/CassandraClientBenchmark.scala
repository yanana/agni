package benchmarks.io

import java.math.BigInteger
import java.time.Instant
import java.util.{ Date, UUID }
import java.util.concurrent.{ Executor, Executors, TimeUnit }

import agni.{ Result, RowDecoder }
import cats.instances.try_._
import cats.instances.list._
import cats.syntax.traverse._
import cats.syntax.cartesian._
import com.datastax.driver.core.querybuilder.{ Insert, Select, QueryBuilder => Q }
import com.datastax.driver.core._
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.guava.CaffeinatedGuava
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures }
import org.openjdk.jmh.annotations._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }
import scala.util.Try

@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
abstract class CassandraClientBenchmark {

  implicit val cache: Cache[String, PreparedStatement] =
    CaffeinatedGuava.build(Caffeine.newBuilder())

  object ST extends agni.std.Try

  case class User(
    id: UUID,
    string_column: String,
    map_column: Map[Int, String],
    list_column: List[BigInt],
    vector_column: Vector[Date]
  )

  type UserTuple = (UUID, String, Map[Int, String], List[BigInt], Vector[Date])

  val uuid1 = UUID.randomUUID()
  val uuid2 = UUID.randomUUID()
  val uuid3 = UUID.randomUUID()

  val users: List[User] = List(
    User(uuid1, "a", (1 to 10).map(i => (i, s"$i")).toMap, (1 to 10).map(BigInt.apply).toList, (1 to 10).map(_ => Date.from(Instant.now)).toVector),
    User(uuid2, "b", (1 to 100).map(i => (i, s"$i")).toMap, (1 to 100).map(BigInt.apply).toList, (1 to 100).map(_ => Date.from(Instant.now)).toVector),
    User(uuid3, "c", (1 to 1000).map(i => (i, s"$i")).toMap, (1 to 1000).map(BigInt.apply).toList, (1 to 1000).map(_ => Date.from(Instant.now)).toVector)
  )

  implicit def buildStatement(s: String): RegularStatement = new SimpleStatement(s)

  val keyspace = "agni_bench"

  def remake: Try[Unit] = for {
    _ <- ST.get[Unit](s"""CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }""".stripMargin)
    _ <- ST.get[Unit](s"USE $keyspace")
    _ <- ST.get[Unit](s"DROP TABLE IF EXISTS user")
    _ <- ST.get[Unit](s"""CREATE TABLE user (id uuid PRIMARY KEY, string_column ascii, map_column map<int, ascii>, list_column list<varint>, vector_column list<timestamp>)""".stripMargin)
  } yield ()

  def insertUserQuery: Insert = Q.insertInto("user")
    .value("id", Q.bindMarker())
    .value("string_column", Q.bindMarker())
    .value("map_column", Q.bindMarker())
    .value("list_column", Q.bindMarker())
    .value("vector_column", Q.bindMarker())

  def insertUser(p: PreparedStatement, a: User): Try[Unit] = for {
    b <- ST.bind(p, a)
    _ <- ST.get[Unit](b)
  } yield ()

  def insert: Try[Unit] = for {
    pstmt <- ST.prepare(insertUserQuery)
    _ <- users.traverse(insertUser(pstmt.setIdempotent(true).setConsistencyLevel(ConsistencyLevel.ALL), _))
  } yield ()

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

  def action: Try[Unit] = for {
    _ <- remake
    _ <- insert
  } yield ()

  var cluster: Cluster = _
  implicit var session: Session = _

  @Setup()
  def setup(): Unit = {
    cluster = Cluster.builder()
      .addContactPoint(sys.env.getOrElse("CASSANDRA_HOST", "localhost"))
      .withPort(sys.env.getOrElse("CASSANDRA_PORT", "9042").toInt)
      .withProtocolVersion(ProtocolVersion.V3)
      .build()
    session = cluster.connect()
    action.get
    session.close()
    session = cluster.connect(keyspace)
  }

  @TearDown
  def tearDown(): Unit = {
    cache.cleanUp()
    cluster.close()
  }
}

@State(Scope.Benchmark)
class AgniBenchmark extends CassandraClientBenchmark {

  val selectUsers: Select =
    Q.select(
      "id",
      "string_column",
      "map_column",
      "list_column",
      "vector_column"
    ).from("user")

  @Benchmark
  def javaStream: java.util.stream.Stream[Row] = {
    val f: Try[java.util.stream.Stream[Row]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[java.util.stream.Stream[Row]](b)
    } yield l
    f.get
  }

  @Benchmark
  def iterToList: List[User] = {
    val f: Try[List[User]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[Iterator[Row]](b)
    } yield {
      val A = RowDecoder.apply[User]
      l.map { row =>
        val v = session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersion
        val user: Result[User] = A.apply(row, v)
        user.fold(throw _, identity)
      }.toList
    }
    f.get
  }

  @Benchmark
  def list: List[User] = {
    val f: Try[List[User]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[List[User]](b)
    } yield l
    f.get
  }

  @Benchmark
  def vector: Vector[User] = {
    val f: Try[Vector[User]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[Vector[User]](b)
    } yield l
    f.get
  }

  @Benchmark
  def listTuple: List[UserTuple] = {
    val f: Try[List[UserTuple]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[List[UserTuple]](b)
    } yield l
    f.get
  }

  @Benchmark
  def vectorTuple: Vector[UserTuple] = {
    val f: Try[Vector[UserTuple]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[Vector[UserTuple]](b)
    } yield l
    f.get
  }

  @Benchmark
  def one: Option[User] = {
    val f: Try[Option[User]] = for {
      p <- ST.prepare(selectUser)
      b <- ST.bind(p, uuid1)
      l <- ST.get[Option[User]](b)
    } yield l
    f.get
  }

  @Benchmark
  def oneTuple: Option[UserTuple] = {
    val f: Try[Option[UserTuple]] = for {
      p <- ST.prepare(selectUser)
      b <- ST.bind(p, uuid1)
      l <- ST.get[Option[UserTuple]](b)
    } yield l
    f.get
  }
}

@State(Scope.Benchmark)
class StdTryBenchmark extends CassandraClientBenchmark {

  @inline final def get(uuid: UUID): Try[Option[User]] = for {
    p <- ST.prepare(selectUser)
    b <- ST.bind(p, uuid)
    l <- ST.get[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] =
    get(uuid1).get

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = get(uuid1)
    val fb = get(uuid2)
    val fc = get(uuid3)

    val f = (fa |@| fb |@| fc).tupled

    f.get
  }
}

@State(Scope.Benchmark)
class StdFutureBenchmark extends CassandraClientBenchmark {

  implicit val context: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  object SF extends agni.std.Future

  import SF.F

  @inline final def getAsync(uuid: UUID): Future[Option[User]] = for {
    p <- SF.prepare(selectUser)
    b <- SF.bind(p, uuid)
    l <- SF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    Await.result(f, Duration.Inf)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)

    val f = (fa |@| fb |@| fc).tupled

    Await.result(f, Duration.Inf)
  }
}

@State(Scope.Benchmark)
class TwitterFutureBenchmark extends CassandraClientBenchmark {
  import com.twitter.util.{ Await => TAwait, Future => TFuture }

  implicit val executor: Executor = Executors.newWorkStealingPool()

  object TF extends agni.twitter.util.Future

  import TF.F

  @inline final def getAsync(uuid: UUID): TFuture[Option[User]] = for {
    p <- TF.prepare(selectUser)
    b <- TF.bind(p, uuid)
    l <- TF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    TAwait.result(f)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)

    val f = (fa |@| fb |@| fc).tupled

    TAwait.result(f)
  }
}

@State(Scope.Benchmark)
class TwitterTryBenchmark extends CassandraClientBenchmark {
  import com.twitter.util.{ Try => TTry }
  object TT extends agni.twitter.util.Try

  import TT.F

  @inline final def get(uuid: UUID): TTry[Option[User]] = for {
    p <- TT.prepare(selectUser)
    b <- TT.bind(p, uuid)
    l <- TT.get[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] =
    get(uuid1).get

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = get(uuid1)
    val fb = get(uuid2)
    val fc = get(uuid3)

    val f = (fa |@| fb |@| fc).tupled

    f.get
  }
}

@State(Scope.Benchmark)
class MonixTaskBenchmark extends CassandraClientBenchmark {
  import monix.eval.{ Task => MTask }
  import monix.execution.Scheduler
  import scala.concurrent.duration._

  implicit val scheduler: Scheduler =
    Scheduler.computation()

  object MF extends agni.monix.Task

  import MF.F

  @inline final def getAsync(uuid: UUID): MTask[Option[User]] = for {
    p <- MF.prepare(selectUser)
    b <- MF.bind(p, uuid)
    l <- MF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    Await.result(f.runAsync, 10.seconds)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)

    val f = (fa |@| fb |@| fc).tupled

    Await.result(f.runAsync, 10.seconds)
  }
}

@State(Scope.Benchmark)
class FS2TaskBenchmark extends CassandraClientBenchmark {
  import fs2.{ Strategy, Task => FTask }
  import scala.concurrent.duration._

  implicit val strategy: Strategy =
    Strategy.fromExecutor(Executors.newWorkStealingPool())

  object FF extends agni.fs2.Task

  import FF.F

  @inline final def getAsync(uuid: UUID): FTask[Option[User]] = for {
    p <- FF.prepare(selectUser)
    b <- FF.bind(p, uuid)
    l <- FF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    Await.result(f.unsafeRunAsyncFuture(), 10.seconds)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)

    val f = (fa |@| fb |@| fc).tupled

    Await.result(f.unsafeRunAsyncFuture(), 10.seconds)
  }
}

@State(Scope.Benchmark)
class JavaDriverFutureBenchmark extends CassandraClientBenchmark {

  implicit val context: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  import cats.instances.future._

  @inline final def getAsync(uuid: UUID): Future[Option[User]] = {
    val p = cache.get(selectUser.toString, () => session.prepare(selectUser))
    val pp = Promise[Option[User]]
    Futures.addCallback(
      session.executeAsync(p.bind(uuid)),
      new FutureCallback[ResultSet] {
        import scala.collection.JavaConverters._
        override def onFailure(t: Throwable): Unit = pp.failure(t)
        override def onSuccess(result: ResultSet): Unit = {
          pp.success(Option(result.one()).map { x =>
            User(
              id = x.getUUID("id"),
              string_column = x.getString("string_column"),
              map_column = x.getMap[Integer, String]("map_column", classOf[Integer], classOf[String]).asScala.toMap.map { case (k, v) => (k: Int, v) },
              list_column = x.getList[BigInteger]("list_column", classOf[BigInteger]).asScala.map(BigInt.apply).toList,
              vector_column = x.getList[Date]("vector_column", classOf[Date]).asScala.toVector
            )
          })
        }
      },
      new Executor {
        override def execute(command: Runnable): Unit = context.execute(command)
      }
    )
    pp.future
  }

  @Benchmark
  def one: Option[User] = {
    Await.result(getAsync(uuid1), Duration.Inf)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)

    val f = (fa |@| fb |@| fc).tupled

    Await.result(f, Duration.Inf)
  }
}
