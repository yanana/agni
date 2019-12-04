package agni

import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.cql.BoundStatement
import shapeless.labelled.FieldType
import shapeless.{ ::, HList, HNil, LabelledGeneric, Lazy, Witness }

trait Binder[A] {
  def apply(bound: BoundStatement, version: ProtocolVersion, a: A): Result[BoundStatement]
}

object Binder extends LowPriorityBinder with TupleBinder {

  def apply[A](implicit A: Binder[A]): Binder[A] = A

  implicit val bindHnil: Binder[HNil] = new Binder[HNil] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, a: HNil): Result[BoundStatement] = Right(bound)
  }

  implicit def bindLabelledHList[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: RowSerializer[H],
    T: Binder[T]
  ): Binder[FieldType[K, H] :: T] =
    new Binder[FieldType[K, H] :: T] {
      override def apply(bound: BoundStatement, version: ProtocolVersion, xs: FieldType[K, H] :: T): Result[BoundStatement] =
        xs match {
          case h :: t => for {
            b <- H(bound, K.value.name, h, version)
            r <- T(b, version, t)
          } yield r
        }
    }

  implicit def bindSingle[A](implicit A: RowSerializer[A]): Binder[A] = new Binder[A] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, a: A): Result[BoundStatement] =
      A.apply(bound, 0, a, version)
  }
}

trait LowPriorityBinder {

  implicit def bindCaseClass[A, R <: HList](
    implicit
    gen: LabelledGeneric.Aux[A, R],
    bind: Lazy[Binder[R]]
  ): Binder[A] =
    new Binder[A] {
      override def apply(bound: BoundStatement, version: ProtocolVersion, a: A): Result[BoundStatement] =
        bind.value.apply(bound, version, gen.to(a))
    }

}
