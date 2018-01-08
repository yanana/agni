package benchmarks.io.sync

import java.util.UUID

import benchmarks.io.DefaultSettings
import cats.instances.try_._
import cats.syntax.apply._
import org.openjdk.jmh.annotations._

import scala.util.Try

abstract class CassandraClientBenchmark extends DefaultSettings

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

    val f = (fa, fb, fc).mapN((_, _, _))

    f.get
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

    val f = (fa, fb, fc).mapN((_, _, _))

    f.get
  }
}
