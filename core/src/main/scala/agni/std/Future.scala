package agni
package std

import java.util.concurrent.Executor

import cats.instances.future._
import com.datastax.driver.core.Statement

import scala.concurrent.{ ExecutionContext, Promise, Future => SFuture }

abstract class Future(implicit ec: ExecutionContext) extends Agni[SFuture, Throwable] {
  type P[A] = Promise[Iterator[A]]
  def executeAsync[A: RowDecoder](query: Statement)(implicit ex: Executor): Action[Iterator[A]] =
    withSession { session =>
      session.executeAsync(query).callback[P, A](
        Promise(), _.failure(_), _.success(_)
      ).future
    }
}
