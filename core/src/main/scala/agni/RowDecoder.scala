package agni

import cats.syntax.either._
import com.datastax.driver.core.{ ProtocolVersion, Row }
import shapeless.{ ::, HList, HNil, LabelledGeneric, Lazy, Witness }
import shapeless.labelled._

trait RowDecoder[A] {
  def apply(row: Row, version: ProtocolVersion): Result[A]
}

object RowDecoder extends LowPriorityRowDecoder with TupleRowDecoder {

  def apply[A](implicit A: RowDecoder[A]): RowDecoder[A] = A

  def unsafeGet[A: RowDecoder](f: Row => Result[A]): RowDecoder[A] =
    new RowDecoder[A] {
      def apply(s: Row, version: ProtocolVersion): Result[A] = f(s)
    }

  implicit val decodeHNil: RowDecoder[HNil] =
    new RowDecoder[HNil] {
      def apply(s: Row, version: ProtocolVersion): Result[HNil] =
        Right(HNil)
    }

  implicit def decodeLabelledHList[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: Lazy[RowDeserializer[H]],
    T: Lazy[RowDecoder[T]]
  ): RowDecoder[FieldType[K, H] :: T] =
    new RowDecoder[FieldType[K, H] :: T] {
      def apply(row: Row, version: ProtocolVersion): Result[FieldType[K, H] :: T] = for {
        h <- H.value.apply(row, K.value.name, version)
        t <- T.value(row, version)
      } yield field[K](h) :: t
    }

  implicit def decodeSingleColumn[A](
    implicit
    A: RowDeserializer[A]
  ): RowDecoder[A] = new RowDecoder[A] {
    def apply(row: Row, version: ProtocolVersion): Result[A] =
      A.apply(row, 0, version)
  }
}

trait LowPriorityRowDecoder {

  implicit def decodeCaseClass[A, R <: HList](
    implicit
    gen: LabelledGeneric.Aux[A, R],
    decode: Lazy[RowDecoder[R]]
  ): RowDecoder[A] =
    new RowDecoder[A] {
      def apply(s: Row, version: ProtocolVersion): Result[A] = decode.value(s, version) map (gen from)
    }
}
