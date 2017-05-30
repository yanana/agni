package agni
package monix

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import _root_.cats.MonadError
import com.datastax.driver.core.{ PreparedStatement, ResultSet, Statement }
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures }
import _root_.monix.cats._
import _root_.monix.execution._
import _root_.monix.eval.{ Coeval, Task => MTask }

abstract class Task(implicit _cache: Cache[String, PreparedStatement])
    extends Agni[MTask, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[MTask, Throwable] = cats.taskToMonadError

  override protected val cache: Cache[String, PreparedStatement] = _cache

  def getAsync[A](stmt: Statement)(implicit ex: Executor, A: Get[A]): Action[A] =
    withSession { session =>
      MTask.async { (_, cb) =>
        val f = session.executeAsync(stmt)
        Futures.addCallback(f, new FutureCallback[ResultSet] {
          def onFailure(t: Throwable): Unit =
            cb.onError(t)

          def onSuccess(result: ResultSet): Unit =
            cb(A.apply[Coeval, Throwable](result, ver(session)))
        }, ex)
        Cancelable()
      }
    }
}
