package agni
package std

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import agni.util.Guava
import cats.MonadError
import cats.instances.future._
import com.datastax.driver.core.{ PreparedStatement, Session, Statement }
import com.google.common.cache.Cache

import scala.concurrent.{ ExecutionContext, Promise, Future => SFuture }

abstract class Future(implicit ec: ExecutionContext, _cache: Cache[String, PreparedStatement])
  extends Async[SFuture, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[SFuture, Throwable] = catsStdInstancesForFuture

  override protected val cache: Cache[String, PreparedStatement] = _cache

  override def getAsync[A: Get](stmt: Statement)(implicit s: Session): SFuture[A] = {
    val p = Promise[A]
    val f = Guava.async[A](
      s.executeAsync(stmt),
      _.fold(p.failure, p.success),
      new Executor {
        override def execute(command: Runnable): Unit = ec.execute(command)
      })
    f(ver(s))
    p.future
  }
}
