package agni.twitter.util

import agni._
import cats.MonadError
import com.twitter.util.{ Future => TFuture }
import io.catbird.util.futureInstance

object Future extends Agni[TFuture, Throwable] {
  implicit val twitterFutureMonadError = MonadError[TFuture, Throwable]
}
