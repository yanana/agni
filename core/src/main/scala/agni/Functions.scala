package agni

import com.datastax.driver.core.Session
import cats.data.{ Xor, Kleisli }

trait Functions {
  def withSession[F[_], A](f: Session => F[A]): Action[F, A] = Action(f)
}
