package benchmarks.inMem

import java.util.concurrent.TimeUnit

import agni.Get
import benchmarks.inMem.cassandra.AResultSet
import cats.instances.try_._
import com.datastax.driver.core.ResultSet
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 5)
@Measurement(iterations = 10, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
class GetBenchmark {
  import GetBenchmark._

  @Benchmark
  def getRow100AsList(data: Data): List[C5] =
    Get[List[C5]].apply[scala.util.Try, Throwable](data.row100, data.v).get

  @Benchmark
  def getRow1000AsList(data: Data): List[C5] =
    Get[List[C5]].apply[scala.util.Try, Throwable](data.row1000, data.v).get

  @Benchmark
  def getRow5000AsList(data: Data): List[C5] =
    Get[List[C5]].apply[scala.util.Try, Throwable](data.row5000, data.v).get

  @Benchmark
  def getRow100AsVector(data: Data): Vector[C5] =
    Get[Vector[C5]].apply[scala.util.Try, Throwable](data.row100, data.v).get

  @Benchmark
  def getRow1000AsVector(data: Data): Vector[C5] =
    Get[Vector[C5]].apply[scala.util.Try, Throwable](data.row1000, data.v).get

  @Benchmark
  def getRow5000AsVector(data: Data): Vector[C5] =
    Get[Vector[C5]].apply[scala.util.Try, Throwable](data.row5000, data.v).get
}

object GetBenchmark {
  @State(Scope.Benchmark)
  class Data extends Base {
    val row100: ResultSet = new AResultSet((1 to 100).map(_ => row))
    val row1000: ResultSet = new AResultSet((1 to 1000).map(_ => row))
    val row5000: ResultSet = new AResultSet((1 to 5000).map(_ => row))
  }
}
