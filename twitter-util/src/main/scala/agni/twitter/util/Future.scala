package agni
package twitter.util

import java.util.concurrent.Executor

import com.datastax.driver.core.Statement
import com.twitter.util.{ Promise, Future => TFuture }

object Future extends Agni[TFuture, Throwable] {
  type P[A] = Promise[Iterator[A]]
  def executeAsync[A](query: Statement)(
    implicit
    decoder: RowDecoder[A],
    executor: Executor
  ): Action[Iterator[A]] =
    withSession { session =>
      session.executeAsync(query).callback[P, A](
        Promise(), _.setException(_), _.setValue(_)
      )
    }
}
