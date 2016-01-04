package agni

import com.datastax.driver.core.Session
import cats.data.Kleisli

trait Functions {
  def withSession[F[_], U](f: Session => F[U]): Action[F, U] = Kleisli.function[F, Session, U](f)
}
