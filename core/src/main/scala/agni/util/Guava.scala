package agni
package util

import java.util.concurrent.Executor

import cats.instances.either._
import cats.syntax.either._
import com.datastax.driver.core._
import com.google.common.util.concurrent.{ FutureCallback, Futures }

object Guava {

  def async[A: Get](
    rsf: ResultSetFuture,
    cb: Either[Throwable, A] => Unit,
    ex: Executor): ProtocolVersion => Unit = pVer =>
    Futures.addCallback(
      rsf,
      new FutureCallback[ResultSet] {
        def onFailure(t: Throwable): Unit =
          cb(t.asLeft)

        def onSuccess(result: ResultSet): Unit =
          cb(Get[A].apply[Either[Throwable, ?], Throwable](result, pVer))
      },
      ex)
}
