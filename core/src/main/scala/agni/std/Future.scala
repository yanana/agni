package agni
package std

import java.util.concurrent.Executor

import cats.instances.future._
import com.datastax.driver.core.{ ResultSet, Statement }
import com.google.common.util.concurrent.{ FutureCallback, Futures }

import scala.concurrent.{ ExecutionContext, Promise, Future => SFuture }

abstract class Future(implicit ec: ExecutionContext) extends Agni[SFuture, Throwable] {

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
