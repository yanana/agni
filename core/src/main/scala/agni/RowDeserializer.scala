package agni

import cats.syntax.either._
import com.datastax.driver.core.{ ProtocolVersion, Row, TupleValue, UDTValue }

trait RowDeserializer[A] {
  def apply(row: Row, i: Int, version: ProtocolVersion): Result[A]
  def apply(row: Row, name: String, version: ProtocolVersion): Result[A]
}

object RowDeserializer {
  def apply[A](implicit A: RowDeserializer[A]): RowDeserializer[A] = A

  implicit def builtIn[A](implicit A: Deserializer[A]): RowDeserializer[A] = new RowDeserializer[A] {
    override def apply(row: Row, i: Int, version: ProtocolVersion): Result[A] = for {
      v <- Either.catchNonFatal(row.getBytesUnsafe(i))
      r <- A(v, version)
    } yield r

    override def apply(row: Row, name: String, version: ProtocolVersion): Result[A] = for {
      v <- Either.catchNonFatal(row.getBytesUnsafe(name))
      r <- A(v, version)
    } yield r
  }

  implicit val tupleValue: RowDeserializer[TupleValue] = new RowDeserializer[TupleValue] {
    override def apply(row: Row, i: Int, version: ProtocolVersion): Result[TupleValue] =
      row.getTupleValue(i).asRight

    override def apply(row: Row, name: String, version: ProtocolVersion): Result[TupleValue] =
      row.getTupleValue(name).asRight
  }

  implicit val udtValue: RowDeserializer[UDTValue] = new RowDeserializer[UDTValue] {
    override def apply(row: Row, i: Int, version: ProtocolVersion): Result[UDTValue] =
      Either.catchNonFatal(row.getUDTValue(i))

    override def apply(row: Row, name: String, version: ProtocolVersion): Result[UDTValue] =
      Either.catchNonFatal(row.getUDTValue(name))
  }
}
