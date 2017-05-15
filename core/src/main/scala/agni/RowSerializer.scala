package agni

import cats.syntax.either._
import com.datastax.driver.core._

trait RowSerializer[A] {
  def apply(bound: BoundStatement, i: Int, a: A, version: ProtocolVersion): Result[BoundStatement]
  def apply(bound: BoundStatement, name: String, a: A, version: ProtocolVersion): Result[BoundStatement]
}

object RowSerializer {
  def apply[A](implicit A: RowSerializer[A]): RowSerializer[A] = A

  implicit def builtIn[A](implicit A: Serializer[A]): RowSerializer[A] = new RowSerializer[A] {
    override def apply(bound: BoundStatement, i: Int, a: A, version: ProtocolVersion): Result[BoundStatement] = for {
      bs <- A.apply(a, version)
      _ <- Either.catchNonFatal(bound.setBytesUnsafe(i, bs))
    } yield bound

    override def apply(bound: BoundStatement, name: String, a: A, version: ProtocolVersion): Result[BoundStatement] = for {
      bs <- A.apply(a, version)
      _ <- Either.catchNonFatal(bound.setBytesUnsafe(name, bs))
    } yield bound
  }

  implicit val tupleValue: RowSerializer[TupleValue] = new RowSerializer[TupleValue] {
    override def apply(bound: BoundStatement, i: Int, a: TupleValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setTupleValue(i, a))

    override def apply(bound: BoundStatement, name: String, a: TupleValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setTupleValue(name, a))
  }

  implicit val udtValue: RowSerializer[UDTValue] = new RowSerializer[UDTValue] {
    override def apply(bound: BoundStatement, i: Int, a: UDTValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setUDTValue(i, a))

    override def apply(bound: BoundStatement, name: String, a: UDTValue, version: ProtocolVersion): Result[BoundStatement] =
      Either.catchNonFatal(bound.setUDTValue(name, a))
  }
}
