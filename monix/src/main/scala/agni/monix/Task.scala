package agni
package monix

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import agni.util.Guava
import com.datastax.driver.core._
import com.google.common.cache.Cache
import _root_.cats.MonadError
import _root_.monix.execution._
import _root_.monix.eval.{ Task => MTask }

abstract class Task(implicit _cache: Cache[String, PreparedStatement])
  extends Async[MTask, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[MTask, Throwable] = cats.taskToMonadError

  override protected val cache: Cache[String, PreparedStatement] = _cache

  override def getAsync[A: Get](stmt: Statement)(implicit s: Session): MTask[A] =
    MTask.async { (scheduler, cb) =>
      val f = Guava.async[A](
        s.executeAsync(stmt),
        _.fold(cb.onError, cb.onSuccess),
        new Executor {
          override def execute(command: Runnable): Unit =
            scheduler.execute(command)
        })
      f(ver(s))
      Cancelable()
    }
}

object Task {
  implicit def monixTaskInstance(implicit cache: Cache[String, PreparedStatement]): Async[MTask, Throwable] = new Task() {}
}
