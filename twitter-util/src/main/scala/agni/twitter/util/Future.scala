package agni
package twitter.util

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import com.datastax.driver.core.{ PreparedStatement, ResultSet, Statement }
import com.google.common.cache.Cache
import com.google.common.util.concurrent.{ FutureCallback, Futures }
import com.twitter.util.{ Promise, Future => TFuture }
import io.catbird.util._

abstract class Future(implicit _cache: Cache[String, PreparedStatement])
    extends Agni[TFuture, Throwable] with CachedPreparedStatementWithGuava {

  override protected val cache: Cache[String, PreparedStatement] = _cache

  type P[A] = Promise[Iterator[Result[A]]]

  @Deprecated
  def executeAsync[A: RowDecoder](query: Statement)(implicit ex: Executor): Action[Iterator[Result[A]]] =
    withSession(_.executeAsync(query).callback[P, A](
      Promise(), _.setException(_), _.setValue(_)
    ))

  def getAsync[A](stmt: Statement)(implicit ex: Executor, A: Get[A]): Action[A] =
    withSession { s =>
      val p = Promise[A]
      val f = s.executeAsync(stmt)
      Futures.addCallback(f, new FutureCallback[ResultSet] {
        def onFailure(t: Throwable): Unit =
          p.setException(t)
        def onSuccess(result: ResultSet): Unit =
          p.become(A.apply[TFuture, Throwable](result))
      }, ex)
      p
    }
}
