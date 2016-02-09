import cats.data.Kleisli
import com.datastax.driver.core.Session

package object agni {

  type Action[F[_], A] = Kleisli[F, Session, A]
  val Action = Kleisli

}
