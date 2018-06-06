package benchmarks.inMem

import java.util.concurrent.TimeUnit

import agni.RowDecoder
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 5)
@Measurement(iterations = 10, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
class RowDecoderBenchmark {
  import RowDecoderBenchmark._

  @Benchmark
  def stringTuple5(data: Data): Either[Throwable, S5] =
    RowDecoder[S5].apply(data.row, data.v)

  @Benchmark
  def stringCaseClass5(data: Data): Either[Throwable, C5] =
    RowDecoder[C5].apply(data.row, data.v)

  @Benchmark
  def stringTuple22(data: Data): Either[Throwable, S22] =
    RowDecoder[S22].apply(data.row, data.v)

  @Benchmark
  def stringCaseClass22(data: Data): Either[Throwable, C22] =
    RowDecoder[C22].apply(data.row, data.v)

  @Benchmark
  def stringCaseClass30(data: Data): Either[Throwable, C30] =
    RowDecoder[C30].apply(data.row, data.v)

  @Benchmark
  def stringTuple5CachedDecoder(data: Data): Either[Throwable, S5] =
    data.decodeS5(data.row, data.v)

  @Benchmark
  def stringCaseClass5CachedDecoder(data: Data): Either[Throwable, C5] =
    data.decodeC5(data.row, data.v)

  @Benchmark
  def stringTuple22CachedDecoder(data: Data): Either[Throwable, S22] =
    data.decodeS22(data.row, data.v)

  @Benchmark
  def stringCaseClass22CachedDecoder(data: Data): Either[Throwable, C22] =
    data.decodeC22(data.row, data.v)

  @Benchmark
  def stringCaseClass30CachedDecoder(data: Data): Either[Throwable, C30] =
    data.decodeC30(data.row, data.v)
}

object RowDecoderBenchmark {

  @State(Scope.Benchmark)
  class Data extends Base {
    final val decodeS5: RowDecoder[S5] = RowDecoder[S5]
    final val decodeS22: RowDecoder[S22] = RowDecoder[S22]
    final val decodeC5: RowDecoder[C5] = RowDecoder[C5]
    final val decodeC22: RowDecoder[C22] = RowDecoder[C22]
    final val decodeC30: RowDecoder[C30] = RowDecoder[C30]
  }
}
