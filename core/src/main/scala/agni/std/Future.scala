package agni.std

import agni.Agni
import cats.MonadError
import cats.std.future._

import scala.concurrent.{ ExecutionContext, Future => SFuture }

object Future extends Agni[SFuture, Throwable] {
  implicit def scalaFutureMonadError(implicit EC: ExecutionContext) = MonadError[SFuture, Throwable]
}
