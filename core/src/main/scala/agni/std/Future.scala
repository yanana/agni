package agni
package std

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import cats.MonadError
import cats.instances.future._
import com.datastax.driver.core._
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures }

import scala.concurrent.{ ExecutionContext, Promise, Future => SFuture }

abstract class Future(implicit ec: ExecutionContext, _cache: Cache[String, PreparedStatement])
  extends Agni[SFuture, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[SFuture, Throwable] = catsStdInstancesForFuture

  override protected val cache: Cache[String, PreparedStatement] = _cache

  def getAsync[A: Get](stmt: Statement): Action[A] =
    withSession { s =>
      val p = Promise[A]
      Futures.addCallback(
        s.executeAsync(stmt),
        new FutureCallback[ResultSet] {
          def onFailure(t: Throwable): Unit =
            p.failure(t)

          def onSuccess(result: ResultSet): Unit =
            p.completeWith(Get[A].apply[SFuture, Throwable](result, ver(s)))
        },
        new Executor {
          override def execute(command: Runnable): Unit =
            ec.execute(command)
        })

      p.future
    }
}
