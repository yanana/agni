package agni.twitter.util

import agni._
import cats.MonadError
import com.twitter.util.{ Try => TTry }
import io.catbird.util.tryInstance

object Try extends Agni[TTry, Throwable] {
  implicit val twitterTryMonadError = MonadError[TTry, Throwable]
}
