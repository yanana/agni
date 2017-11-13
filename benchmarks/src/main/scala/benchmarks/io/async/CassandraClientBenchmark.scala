package benchmarks.io.async

import java.math.BigInteger
import java.util.concurrent.{ Executor, Executors }
import java.util.{ Date, UUID }

import benchmarks.io.DefaultSettings
import cats.syntax.cartesian._
import com.datastax.driver.core._
import com.google.common.util.concurrent.{ FutureCallback, Futures }
import org.openjdk.jmh.annotations._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }

abstract class CassandraClientBenchmark extends DefaultSettings

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

    val f = (fa |@| fb |@| fc).map((_, _, _))

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

    val f = (fa |@| fb |@| fc).map((_, _, _))

    TAwait.result(f)
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

    val f = (fa |@| fb |@| fc).map((_, _, _))

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

    val f = (fa |@| fb |@| fc).map((_, _, _))

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

    val f = (fa |@| fb |@| fc).map((_, _, _))

    Await.result(f, Duration.Inf)
  }
}
