package agni

import cats.data.Kleisli
import com.datastax.driver.core.Session

trait Functions[F[_]] {
  type Action[A] = Kleisli[F, Session, A]
  def withSession[A](f: Session => F[A]): Action[A] = Kleisli(f)
}
