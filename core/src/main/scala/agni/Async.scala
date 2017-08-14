package agni

import com.datastax.driver.core._

trait Async[F[_], E] extends Agni[F, E] {
  self: GetPreparedStatement =>

  def getAsync[A: Get](stmt: Statement): Action[A]
}
