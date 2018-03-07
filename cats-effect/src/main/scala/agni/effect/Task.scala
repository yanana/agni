package agni
package effect

import agni.cache.CachedPreparedStatementWithGuava
import agni.util.Guava
import cats.MonadError
import com.datastax.driver.core._
import com.google.common.cache.Cache
import cats.effect.{ Async => EffAsync }
import com.google.common.util.concurrent.MoreExecutors

abstract class Task[F[_]](implicit FF: EffAsync[F], _cache: Cache[String, PreparedStatement])
  extends Async[F, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[F, Throwable] = implicitly

  override protected val cache: Cache[String, PreparedStatement] = _cache

  def getAsync[A: Get](stmt: Statement)(implicit s: Session): F[A] =
    FF.async[A] { cb =>
      val f = Guava.async[A](
        s.executeAsync(stmt),
        cb,
        MoreExecutors.directExecutor()
      )
      f(ver(s))
    }
}

object Task {
  implicit def catsEffectTaskInstance[F[_]](implicit F: EffAsync[F], cache: Cache[String, PreparedStatement]): Async[F, Throwable] = new Task[F]() {}
}
