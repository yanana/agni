package benchmarks.io

import java.math.BigInteger
import java.time.Instant
import java.util.{ Date, UUID }
import java.util.concurrent.{ Executor, Executors, TimeUnit }

import agni.{ Result, RowDecoder }
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

@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
abstract class CassandraClientBenchmark {

  implicit val executor: Executor = MoreExecutors.directExecutor()

  implicit val context: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

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
    User(uuid1, "a", (1 to 10).map(i => (i, "$i")).toMap, (1 to 10).map(BigInt.apply).toList, (1 to 10).map(_ => Date.from(Instant.now)).toVector),
    User(uuid2, "b", (1 to 100).map(i => (i, "$i")).toMap, (1 to 100).map(BigInt.apply).toList, (1 to 100).map(_ => Date.from(Instant.now)).toVector),
    User(uuid3, "c", (1 to 1000).map(i => (i, "$i")).toMap, (1 to 1000).map(BigInt.apply).toList, (1 to 1000).map(_ => Date.from(Instant.now)).toVector)
  )

  implicit def buildStatement(s: String): RegularStatement = new SimpleStatement(s)

  val keyspace = "agni_bench"

  val remake: ST.Action[Unit] = for {
    _ <- ST.get[Unit](s"""CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }""".stripMargin)
    _ <- ST.get[Unit](s"USE $keyspace")
    _ <- ST.get[Unit](s"DROP TABLE IF EXISTS user")
    _ <- ST.get[Unit](s"""CREATE TABLE user (id uuid PRIMARY KEY, string_column ascii, map_column map<int, ascii>, list_column list<varint>, vector_column list<timestamp>)""".stripMargin)
  } yield ()

  val insertUserQuery: Insert = Q.insertInto("user")
    .value("id", Q.bindMarker())
    .value("string_column", Q.bindMarker())
    .value("map_column", Q.bindMarker())
    .value("list_column", Q.bindMarker())
    .value("vector_column", Q.bindMarker())

  def insertUser(p: PreparedStatement, a: User): ST.Action[Unit] = for {
    b <- ST.bind(p, a)
    _ <- ST.get[Unit](b)
  } yield ()

  val insert: ST.Action[Unit] = for {
    pstmt <- ST.prepare(insertUserQuery)
    _ <- users.traverse(insertUser(pstmt.setIdempotent(true).setConsistencyLevel(ConsistencyLevel.ALL), _))
  } yield ()

  val selectUser: Select.Where = Q.select.from("user").where(Q.eq("id", Q.bindMarker()))

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
    cache.cleanUp()
    cluster.close()
  }
}

@State(Scope.Thread)
class AgniBenchmark extends CassandraClientBenchmark {

  val selectUsers: Select = Q.select("id", "string_column", "map_column", "list_column", "vector_column").from("user")

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
    val f: ST.Action[List[User]] = for {
      p <- ST.prepare(selectUsers)
      b <- ST.bind(p, ())
      l <- ST.get[Iterator[Row]](b)
    } yield {
      val A = RowDecoder.apply[User]
      l.map { row =>
        val user: Result[User] = A.apply(row, session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersion)
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

  @Benchmark
  def one: Option[User] = {
    val f: ST.Action[Option[User]] = for {
      p <- ST.prepare(selectUser)
      b <- ST.bind(p, uuid1)
      l <- ST.get[Option[User]](b)
    } yield l
    f.run(session).get
  }

  @Benchmark
  def oneTuple: Option[UserTuple] = {
    val f: ST.Action[Option[UserTuple]] = for {
      p <- ST.prepare(selectUser)
      b <- ST.bind(p, uuid1)
      l <- ST.get[Option[UserTuple]](b)
    } yield l
    f.run(session).get
  }
}

@State(Scope.Thread)
class StdTryBenchmark extends CassandraClientBenchmark {

  @inline def get: ST.Action[Option[User]] = for {
    p <- ST.prepare(selectUser)
    b <- ST.bind(p, uuid1)
    l <- ST.get[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] =
    get.run(session).get

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = get
    val fb = get
    val fc = get

    val f = (fa |@| fb |@| fc).tupled

    f.run(session).get
  }
}

@State(Scope.Thread)
class StdFutureBenchmark extends CassandraClientBenchmark {

  object SF extends agni.std.Future

  @inline def getAsync(uuid: UUID): SF.Action[Option[User]] = for {
    p <- SF.prepare(selectUser)
    b <- SF.bind(p, uuid)
    l <- SF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    Await.result(f.run(session), Duration.Inf)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)
    val f = (fa |@| fb |@| fc).tupled

    Await.result(f.run(session), Duration.Inf)
  }
}

@State(Scope.Thread)
class TwitterFutureBenchmark extends CassandraClientBenchmark {
  import com.twitter.util.{ Await => TAwait }
  import io.catbird.util.twitterFutureInstance

  object TF extends agni.twitter.util.Future

  @inline def getAsync(uuid: UUID): TF.Action[Option[User]] = for {
    p <- TF.prepare(selectUser)
    b <- TF.bind(p, uuid)
    l <- TF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    TAwait.result(f.run(session))
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)
    val f = (fa |@| fb |@| fc).tupled

    TAwait.result(f.run(session))
  }
}

@State(Scope.Thread)
class MonixTaskBenchmark extends CassandraClientBenchmark {
  import agni.monix.cats._
  import monix.execution.Scheduler
  import scala.concurrent.duration._

  object MF extends agni.monix.Task

  implicit val sche: Scheduler = Scheduler.apply(context)

  @inline def getAsync(uuid: UUID): MF.Action[Option[User]] = for {
    p <- MF.prepare(selectUser)
    b <- MF.bind(p, uuid)
    l <- MF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    Await.result(f.run(session).runAsync, 10.seconds)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)
    val f = (fa |@| fb |@| fc).tupled

    Await.result(f.run(session).runAsync, 10.seconds)
  }
}

@State(Scope.Thread)
class FS2TaskBenchmark extends CassandraClientBenchmark {
  import fs2.interop.cats._
  import fs2.Strategy
  import scala.concurrent.duration._

  object FF extends agni.fs2.Task

  implicit val sche: Strategy = Strategy.fromExecutionContext(context)

  @inline def getAsync(uuid: UUID): FF.Action[Option[User]] = for {
    p <- FF.prepare(selectUser)
    b <- FF.bind(p, uuid)
    l <- FF.getAsync[Option[User]](b)
  } yield l

  @Benchmark
  def one: Option[User] = {
    val f = getAsync(uuid1)
    Await.result(f.run(session).unsafeRunAsyncFuture(), 10.seconds)
  }

  @Benchmark
  def three: (Option[User], Option[User], Option[User]) = {
    val fa = getAsync(uuid1)
    val fb = getAsync(uuid2)
    val fc = getAsync(uuid3)
    val f = (fa |@| fb |@| fc).tupled

    Await.result(f.run(session).unsafeRunAsyncFuture(), 10.seconds)
  }
}

@State(Scope.Thread)
class JavaDriverFutureBenchmark extends CassandraClientBenchmark {

  @inline def getAsync(uuid: UUID): Future[Option[User]] = {
    val p = cache.get(selectUser.toString, () => session.prepare(selectUser))
    val pp = Promise[Option[User]]
    Futures.addCallback(session.executeAsync(p.bind(uuid)), new FutureCallback[ResultSet] {
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
    })
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
