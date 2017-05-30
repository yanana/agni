package benchmarks.inMem

import java.util.concurrent.TimeUnit

import agni.RowDecoder
import benchmarks.inMem.cassandra.UndefinedRow
import com.datastax.driver.core.ProtocolVersion
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
class RowDecoderBenchmark {
  private[this] val v = ProtocolVersion.NEWEST_BETA
  private[this] val row = new UndefinedRow {
    override def getString(i: Int): String = "fff"
    override def getString(name: String): String = "ggg"
  }

  @Benchmark def stringTuple5: Either[Throwable, S5] = RowDecoder.apply[S5].apply(row, v)
  @Benchmark def stringCaseClass5: Either[Throwable, C5] = RowDecoder.apply[C5].apply(row, v)
  @Benchmark def stringTuple22: Either[Throwable, S22] = RowDecoder.apply[S22].apply(row, v)
  @Benchmark def stringCaseClass22: Either[Throwable, C22] = RowDecoder.apply[C22].apply(row, v)
  @Benchmark def stringCaseClass30: Either[Throwable, C30] = RowDecoder.apply[C30].apply(row, v)
}
