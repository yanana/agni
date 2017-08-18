package agni
package twitter.util

import java.util.concurrent.Executor

import agni.cache.CachedPreparedStatementWithGuava
import agni.util.Guava
import cats.MonadError
import com.datastax.driver.core._
import com.google.common.cache.Cache
import com.twitter.util.{ Promise, Future => TFuture }
import io.catbird.util._

abstract class Future(implicit ex: Executor, _cache: Cache[String, PreparedStatement])
  extends Async[TFuture, Throwable] with CachedPreparedStatementWithGuava {

  override implicit val F: MonadError[TFuture, Throwable] = twitterFutureInstance

  override protected val cache: Cache[String, PreparedStatement] = _cache

  def getAsync[A: Get](stmt: Statement)(implicit s: Session): TFuture[A] = {
    val p = Promise[A]
    val f = Guava.async[A](
      s.executeAsync(stmt),
      _.fold(p.setException, p.setValue),
      ex)
    f(ver(s))
    p
  }
}
