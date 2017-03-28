package agni

import java.util.concurrent.Executor

import cats.data.Kleisli
import com.datastax.driver.core.{ ResultSet, ResultSetFuture, Session }
import com.google.common.util.concurrent.{ FutureCallback, Futures }

import scala.collection.JavaConverters._

trait Functions[F[_]] {
  type Action[A] = Kleisli[F, Session, A]
  def withSession[A](f: Session => F[A]): Action[A] = Kleisli(f)

  @Deprecated
  implicit class ResultSetFuture0(f: ResultSetFuture) {
    import syntax._

    @Deprecated
    def callback[G[_], A: RowDecoder](p: G[A], fa: (G[A], Throwable) => Unit, fb: (G[A], Iterator[Result[A]]) => Unit)(
      implicit
      ex: Executor
    ): G[A] = {
      Futures.addCallback(f, new FutureCallback[ResultSet] {
        def onFailure(t: Throwable): Unit = fa(p, t)
        def onSuccess(result: ResultSet): Unit = {
          val xs = result.iterator().asScala.map(_.decode)
          fb(p, xs)
        }
      }, ex)
      p
    }
  }
}
