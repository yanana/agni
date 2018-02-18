package benchmarks.io

import java.time.Instant
import java.util.{ Date, UUID }
import java.util.concurrent.TimeUnit

import cats.instances.try_._
import cats.instances.list._
import cats.syntax.traverse._
import com.datastax.driver.core.querybuilder.{ Insert, Select, QueryBuilder => Q }
import com.datastax.driver.core._
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.guava.CaffeinatedGuava
import com.google.common.cache.Cache
import org.openjdk.jmh.annotations._

import scala.util.Try

@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
abstract class DefaultSettings {

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
      .addContactPoint(sys.props.getOrElse("bench.cassandra.host", "localhost"))
      .withPort(sys.props.getOrElse("bench.cassandra.port", "9042").toInt)
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
