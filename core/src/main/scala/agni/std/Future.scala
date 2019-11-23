package agni
package std

import java.util.concurrent.Executor

import agni.util.Futures
import cats.MonadError
import cats.instances.future._
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.BoundStatement

import scala.concurrent.{ ExecutionContext, Promise, Future => SFuture }

abstract class Future(implicit ec: ExecutionContext)
  extends Async[SFuture, Throwable] {

  override implicit val F: MonadError[SFuture, Throwable] = catsStdInstancesForFuture

  override def getAsync[A: Get](stmt: BoundStatement)(implicit s: CqlSession): SFuture[A] = {
    val p = Promise[A]
    val f = Futures.async[A](
      s.executeAsync(stmt),
      _.fold(p.failure, p.success),
      new Executor {
        override def execute(command: Runnable): Unit = ec.execute(command)
      }
    )
    f(ver(s))
    p.future
  }
}
