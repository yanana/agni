package agni
package effect

import java.util.concurrent.Executor

import agni.util.Futures
import cats.MonadError
import cats.effect.{ Async => EffAsync }
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.BoundStatement

abstract class Task[F[_]](implicit FF: EffAsync[F])
  extends Async[F, Throwable] {

  override implicit val F: MonadError[F, Throwable] = implicitly

  private object directExecutor extends Executor {
    def execute(r: Runnable): Unit = r.run()
  }

  def getAsync[A: Get](stmt: BoundStatement)(implicit s: CqlSession): F[A] =
    FF.async[A] { cb =>
      val f = Futures.async[A](
        s.executeAsync(stmt),
        cb,
        directExecutor
      )
      f(ver(s))
    }
}

object Task {
  implicit def catsEffectTaskInstance[F[_]](implicit F: EffAsync[F]): Async[F, Throwable] = new Task[F]() {}
}
