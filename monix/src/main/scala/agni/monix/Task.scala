package agni
package monix

import java.util.concurrent.Executor

import _root_.cats.MonadError
import _root_.monix.eval.{ Task => MTask }
import _root_.monix.execution._
import agni.util.Futures
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.BoundStatement

abstract class Task
  extends Async[MTask, Throwable] {

  override implicit val F: MonadError[MTask, Throwable] = cats.taskToMonadError

  override def getAsync[A: Get](stmt: BoundStatement)(implicit s: CqlSession): MTask[A] =
    MTask.async0 { (scheduler, cb) =>
      val f = Futures.async[A](
        s.executeAsync(stmt),
        _.fold(cb.onError, cb.onSuccess),
        new Executor {
          override def execute(command: Runnable): Unit =
            scheduler.execute(command)
        }
      )
      f(ver(s))
      Cancelable()
    }
}

object Task {
  implicit val monixTaskInstance: Async[MTask, Throwable] = new Task() {}
}
