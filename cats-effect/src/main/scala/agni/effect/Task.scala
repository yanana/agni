package agni
package effect

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import agni.util.Guava
import cats.MonadError
import com.datastax.driver.core._
import com.google.common.cache.Cache
import cats.effect.IO

import scala.concurrent.ExecutionContext

abstract class Task(implicit ec: ExecutionContext, _cache: Cache[String, PreparedStatement])
  extends Async[IO, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[IO, Throwable] = implicitly

  override protected val cache: Cache[String, PreparedStatement] = _cache

  def getAsync[A: Get](stmt: Statement)(implicit s: Session): IO[A] =
    IO.async { cb =>
      val f = Guava.async[A](
        s.executeAsync(stmt),
        cb,
        new Executor {
          def execute(command: Runnable): Unit =
            ec.execute(command)
        }
      )
      f(ver(s))
    }
}

object Task {
  implicit def catsEffectTaskInstance(implicit ec: ExecutionContext, cache: Cache[String, PreparedStatement]): Async[IO, Throwable] = new Task() {}
}
