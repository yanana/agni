package agni

import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.data.{ TupleValue, UdtValue }

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

  implicit val udtValue: RowDeserializer[UdtValue] = new RowDeserializer[UdtValue] {
    override def apply(row: Row, i: Int, version: ProtocolVersion): Result[UdtValue] =
      Either.catchNonFatal(row.getUdtValue(i))

    override def apply(row: Row, name: String, version: ProtocolVersion): Result[UdtValue] =
      Either.catchNonFatal(row.getUdtValue(name))
  }
}
