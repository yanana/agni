package agni
package fs2

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import cats.MonadError
import cats.instances.either._
import cats.syntax.either._
import com.datastax.driver.core.{ PreparedStatement, ResultSet, Statement }
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures }
import _root_.fs2.{ Strategy, Task => FTask }

abstract class Task(implicit _cache: Cache[String, PreparedStatement])
  extends Agni[FTask, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[FTask, Throwable] =
    _root_.fs2.interop.cats.effectToMonadError

  override protected val cache: Cache[String, PreparedStatement] = _cache

  def getAsync[A: Get](stmt: Statement)(implicit strategy: Strategy, ex: Executor): Action[A] =
    withSession { s =>
      FTask.async { cb =>
        Futures.addCallback(
          s.executeAsync(stmt),
          new FutureCallback[ResultSet] {
            def onFailure(t: Throwable): Unit =
              cb(t.asLeft)

            def onSuccess(result: ResultSet): Unit =
              cb(Get[A].apply[Either[Throwable, ?], Throwable](result, ver(s)))
          },
          ex)
      }
    }
}
