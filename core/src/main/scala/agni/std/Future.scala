package agni
package std

import java.util.concurrent.Executor

import com.datastax.driver.core.Statement

import scala.concurrent.{ Promise, Future => SFuture }

object Future extends Agni[SFuture, Throwable] {
  type P[A] = Promise[Iterator[A]]
  def executeAsync[A](query: Statement)(
    implicit
    decoder: RowDecoder[A],
    executor: Executor
  ): Action[Iterator[A]] =
    withSession { session =>
      session.executeAsync(query).callback[P, A](
        Promise(), _.failure(_), _.success(_)
      ).future
    }
}
