package benchmarks.io.free

import java.util.UUID
import java.util.concurrent.{ Executor, Executors }

import _root_.iota.TListK.:::
import _root_.iota.{ CopK, TNilK }
import agni.Get
import agni.free.session._
import benchmarks.io.DefaultSettings
import cats.data.Kleisli
import cats.effect.IO
import cats.free.Free
import cats.{ MonadError, ~> }
import com.datastax.driver.core.Session
import com.twitter.util.{ Await => TAwait, Future => TFuture }
import monix.eval.{ Task => MTask }
import monix.execution.Scheduler
import org.openjdk.jmh.annotations._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

final case class Env(session: Session)

abstract class CassandraClientBenchmark[F[_]] extends DefaultSettings {

  lazy val env: Env = Env(session)

  type G[A] = Kleisli[F, Env, A]

  implicit lazy val getSession: Env => Session = _.session

  implicit def cacheableF: Cacheable[F]

  implicit lazy val sessionOpHandler: SessionOp.Handler[F, Env] =
    SessionOp.Handler.sessionOpHandlerWithJ[F, Env]

  type Algebra[A] = CopK[SessionOp ::: TNilK, A]

  implicit lazy val handler: Algebra ~> G = CopK.FunctionK.summon

  lazy val S: iota.SessionOps[Algebra] = implicitly

  def getUser[A: Get](uuid: UUID): Free[Algebra, A] = for {
    p <- S.prepare(selectUser)
    b <- S.bind(p, uuid)
    l <- S.executeAsync[A](b)
  } yield l

  def interpret[A: Get](program: Free[Algebra, A])(implicit M: MonadError[F, Throwable]): G[A] =
    program.foldMap(handler)

  def run[A: Get](uuid: UUID)(implicit M: MonadError[F, Throwable]): F[A] =
    interpret[A](getUser[A](uuid)).run(env)
}

@State(Scope.Benchmark)
class StdFutureBenchmark extends CassandraClientBenchmark[Future] {
  import cats.instances.future._

  implicit val context: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  implicit val cacheableF: Cacheable[Future] = new agni.std.Future {}

  @Benchmark
  def one: Option[User] =
    Await.result(run[Option[User]](uuid1), Duration.Inf)
}

@State(Scope.Benchmark)
class TwitterFutureBenchmark extends CassandraClientBenchmark[TFuture] {
  import io.catbird.util.twitterFutureInstance

  implicit val executor: Executor = Executors.newWorkStealingPool()

  implicit val cacheableF: Cacheable[TFuture] = new agni.twitter.util.Future {}

  @Benchmark
  def one: Option[User] =
    TAwait.result(run[Option[User]](uuid1))
}

@State(Scope.Benchmark)
class MonixTaskBenchmark extends CassandraClientBenchmark[MTask] {
  import agni.monix.cats.taskToMonadError

  implicit val scheduler: Scheduler =
    Scheduler.computation()

  implicit val cacheableF: Cacheable[MTask] = new agni.monix.Task {}

  @Benchmark
  def one: Option[User] =
    Await.result(run[Option[User]](uuid1).runToFuture, Duration.Inf)
}

@State(Scope.Benchmark)
class CatsEffectTaskBenchmarkF extends CassandraClientBenchmark[IO] {

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  implicit val cacheableF: Cacheable[IO] = new agni.effect.Task[IO] {}

  @Benchmark
  def one: Option[User] =
    Await.result(run[Option[User]](uuid1).unsafeToFuture(), Duration.Inf)
}
