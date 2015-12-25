import com.datastax.driver.core.Session
import cats.data.Kleisli

package object agni {
  type Action[F[_], U] = Kleisli[F, Session, U]
}
