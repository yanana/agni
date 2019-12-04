package agni
package std

import cats.MonadError
import cats.instances.try_._

import scala.util.{ Try => STry }

abstract class Try extends Agni[STry, Throwable] {

  override val F: MonadError[STry, Throwable] = catsStdInstancesForTry
}
