package agni
package twitter.util

import cats.MonadError
import com.twitter.util.{ Try => TTry }
import io.catbird.util._

abstract class Try extends Agni[TTry, Throwable] {

  override implicit val F: MonadError[TTry, Throwable] = twitterTryInstance
}
