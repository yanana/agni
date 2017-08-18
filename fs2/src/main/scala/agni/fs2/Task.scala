package agni
package fs2

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import agni.util.Guava
import cats.MonadError
import com.datastax.driver.core.{ PreparedStatement, Statement }
import com.google.common.cache.Cache
import _root_.fs2.{ Strategy, Task => FTask }

abstract class Task(implicit strategy: Strategy, _cache: Cache[String, PreparedStatement])
  extends Async[FTask, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[FTask, Throwable] =
    _root_.fs2.interop.cats.effectToMonadError

  override protected val cache: Cache[String, PreparedStatement] = _cache

  override def getAsync[A: Get](stmt: Statement): Action[A] =
    withSession { session =>
      FTask.async { cb =>
        val f = Guava.async[A](
          session.executeAsync(stmt),
          cb,
          new Executor {
            override def execute(command: Runnable): Unit =
              strategy(command.run())
          })
        f(ver(session))
      }
    }
}
