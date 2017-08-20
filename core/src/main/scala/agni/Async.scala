package agni

import com.datastax.driver.core._

trait Async[F[_], E] extends Agni[F, E] {
  self: GetPreparedStatement =>

  def getAsync[A: Get](stmt: Statement)(implicit s: Session): F[A]
}

object Async {
  def apply[F[_], E](implicit F: Async[F, E]): Async[F, E] = F
}
