package agni
package std

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import cats.MonadError
import cats.instances.future._
import com.datastax.driver.core.{ PreparedStatement, ResultSet, Statement }
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures }

import scala.concurrent.{ ExecutionContext, Promise, Future => SFuture }

abstract class Future(implicit ec: ExecutionContext, _cache: Cache[String, PreparedStatement])
    extends Agni[SFuture, Throwable] with CachedPreparedStatementWithGuava {

  override val F: MonadError[SFuture, Throwable] = catsStdInstancesForFuture

  override protected val cache: Cache[String, PreparedStatement] = _cache

  type P[A] = Promise[Iterator[Result[A]]]

  @Deprecated
  def executeAsync[A: RowDecoder](query: Statement)(implicit ex: Executor): Action[Iterator[Result[A]]] =
    withSession { session =>
      session.executeAsync(query).callback[P, A](
        Promise(), _.failure(_), _.success(_)
      ).future
    }

  def getAsync[A](stmt: Statement)(implicit ex: Executor, A: Get[A]): Action[A] =
    withSession { s =>
      val p = Promise[A]
      val f = s.executeAsync(stmt)
      Futures.addCallback(f, new FutureCallback[ResultSet] {
        def onFailure(t: Throwable): Unit =
          p.failure(t)
        def onSuccess(result: ResultSet): Unit =
          p.completeWith(A.apply[SFuture, Throwable](result))
      }, ex)
      p.future
    }
}
