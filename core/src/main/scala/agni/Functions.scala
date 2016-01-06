package agni

import com.datastax.driver.core.Session
import cats.data.{ Xor, Kleisli }

trait Functions {
  def withSession[F[_], A](f: Session => F[Xor[Throwable, A]]): Action[F, A] =
    Kleisli.function[Lambda[a => F[Xor[Throwable, a]]], Session, A](f)
}
