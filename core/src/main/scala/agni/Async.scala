package agni

import com.datastax.oss.driver.api.core.cql.BoundStatement
import com.datastax.oss.driver.api.core.CqlSession

trait Async[F[_], E] extends Agni[F, E] {

  def getAsync[A: Get](stmt: BoundStatement)(implicit s: CqlSession): F[A]
}

object Async {
  def apply[F[_], E](implicit F: Async[F, E]): Async[F, E] = F
}
