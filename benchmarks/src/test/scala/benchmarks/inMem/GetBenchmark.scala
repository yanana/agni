package benchmarks.inMem

import java.util.concurrent.TimeUnit

import agni.Get
import benchmarks.inMem.cassandra.{ AResultSet, UndefinedRow }
import cats.instances.try_._
import com.datastax.driver.core.{ ProtocolVersion, ResultSet }
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
class GetBenchmark {
  private[this] val v = ProtocolVersion.NEWEST_BETA
  private[this] val row = new UndefinedRow {
    override def getString(i: Int): String = "fff"
    override def getString(name: String): String = "ggg"
  }

  val row100: ResultSet = new AResultSet((1 to 100).map(_ => row))
  val row1000: ResultSet = new AResultSet((1 to 1000).map(_ => row))
  val row5000: ResultSet = new AResultSet((1 to 5000).map(_ => row))

  @Benchmark def getRow100AsList: List[C5] = Get[List[C5]].apply[scala.util.Try, Throwable](row100, v).get
  @Benchmark def getRow1000AsList: List[C5] = Get[List[C5]].apply[scala.util.Try, Throwable](row1000, v).get
  @Benchmark def getRow5000AsList: List[C5] = Get[List[C5]].apply[scala.util.Try, Throwable](row5000, v).get

  @Benchmark def getRow100AsVector: Vector[C5] = Get[Vector[C5]].apply[scala.util.Try, Throwable](row100, v).get
  @Benchmark def getRow1000AsVector: Vector[C5] = Get[Vector[C5]].apply[scala.util.Try, Throwable](row1000, v).get
  @Benchmark def getRow5000AsVector: Vector[C5] = Get[Vector[C5]].apply[scala.util.Try, Throwable](row5000, v).get
}
