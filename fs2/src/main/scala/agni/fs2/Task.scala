package agni
package fs2

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import cats.MonadError
import cats.instances.try_._
import cats.syntax.either._
import com.datastax.driver.core.{ PreparedStatement, ResultSet, Statement }
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures }
import _root_.fs2.{ Strategy, Task => FTask }

import scala.util.Try

abstract class Task(implicit _cache: Cache[String, PreparedStatement])
    extends Agni[FTask, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[FTask, Throwable] =
    _root_.fs2.interop.cats.effectToMonadError

  override protected val cache: Cache[String, PreparedStatement] = _cache

  def getAsync[A: Get](stmt: Statement)(
    implicit
    strategy: Strategy,
    ex: Executor
  ): Action[A] =
    withSession { s =>
      FTask.async { cb =>
        val f = s.executeAsync(stmt)
        Futures.addCallback(f, new FutureCallback[ResultSet] {
          def onFailure(t: Throwable): Unit =
            cb(t.asLeft)

          def onSuccess(result: ResultSet): Unit =
            cb(Either.fromTry(Get[A].apply[Try, Throwable](result)))
        }, ex)
      }
    }
}
