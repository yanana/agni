package agni
package syntax

import com.datastax.driver.core.Row

trait RowDecoderSyntax {
  implicit def rowDecoderSyntax[A](a: Row): RowDecoderOps[A] = new RowDecoderOps(a)
}

final class RowDecoderOps[A](a: Row) {
  def decode(implicit A: RowDecoder[A]): Result[A] = A(a)
}
