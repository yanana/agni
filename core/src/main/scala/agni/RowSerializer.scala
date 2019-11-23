package agni

import cats.instances.either._
import cats.syntax.either._
import cats.syntax.flatMap._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.cql.BoundStatement
import com.datastax.oss.driver.api.core.data.{ TupleValue, UdtValue }

trait RowSerializer[A] {
  def apply(bound: BoundStatement, i: Int, a: A, version: ProtocolVersion): Result[BoundStatement]

  def apply(bound: BoundStatement, name: String, a: A, version: ProtocolVersion): Result[BoundStatement]
}

object RowSerializer {
  def apply[A](implicit A: RowSerializer[A]): RowSerializer[A] = A

  implicit def builtIn[A](implicit A: Serializer[A]): RowSerializer[A] = new RowSerializer[A] {
    override def apply(bound: BoundStatement, i: Int, a: A, version: ProtocolVersion): Result[BoundStatement] =
      A.apply(a, version) >>=
        (bs => Either.catchNonFatal(bound.setBytesUnsafe(i, bs)))

    override def apply(bound: BoundStatement, name: String, a: A, version: ProtocolVersion): Result[BoundStatement] =
      A.apply(a, version) >>=
        (bs => Either.catchNonFatal(bound.setBytesUnsafe(name, bs)))
  }

  implicit val tupleValue: RowSerializer[TupleValue] = new RowSerializer[TupleValue] {
    override def apply(bound: BoundStatement, i: Int, a: TupleValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setTupleValue(i, a))

    override def apply(bound: BoundStatement, name: String, a: TupleValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setTupleValue(name, a))
  }

  implicit val udtValue: RowSerializer[UdtValue] = new RowSerializer[UdtValue] {
    override def apply(bound: BoundStatement, i: Int, a: UdtValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setUdtValue(i, a))

    override def apply(bound: BoundStatement, name: String, a: UdtValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setUdtValue(name, a))
  }
}
