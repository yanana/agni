package agni
package twitter.util

import java.util.concurrent.Executor

import agni.util.Futures
import cats.MonadError
import com.twitter.util.{ Promise, Future => TFuture }
import io.catbird.util._
import com.datastax.oss.driver.api.core.cql.BoundStatement
import com.datastax.oss.driver.api.core.CqlSession

abstract class Future(implicit ex: Executor)
  extends Async[TFuture, Throwable] {

  override implicit val F: MonadError[TFuture, Throwable] = twitterFutureInstance

  def getAsync[A: Get](stmt: BoundStatement)(implicit s: CqlSession): TFuture[A] = {
    val p = Promise[A]
    val f = Futures.async[A](
      s.executeAsync(stmt),
      _.fold(p.setException, p.setValue),
      ex
    )
    f(ver(s))
    p
  }
}
