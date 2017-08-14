package agni
package monix

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import agni.util.Guava
import com.datastax.driver.core.{ PreparedStatement, Statement }
import com.google.common.cache.Cache
import _root_.cats.MonadError
import _root_.monix.execution._
import _root_.monix.eval.{ Task => MTask }

abstract class Task(implicit _cache: Cache[String, PreparedStatement])
  extends Async[MTask, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[MTask, Throwable] = cats.taskToMonadError

  override protected val cache: Cache[String, PreparedStatement] = _cache

  override def getAsync[A: Get](stmt: Statement): Action[A] =
    withSession { session =>
      MTask.async { (s, cb) =>
        val f = Guava.async[A](
          session.executeAsync(stmt),
          _.fold(cb.onError, cb.onSuccess),
          new Executor {
            override def execute(command: Runnable): Unit = s.execute(command)
          })
        f(ver(session))
        Cancelable()
      }
    }
}
