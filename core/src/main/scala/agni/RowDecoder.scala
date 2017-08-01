package agni

import cats.instances.either._
import cats.syntax.either._
import cats.syntax.cartesian._
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
    T: Lazy[RowDecoder[T]]): RowDecoder[FieldType[K, H] :: T] =
    new RowDecoder[FieldType[K, H] :: T] {
      def apply(row: Row, version: ProtocolVersion): Result[FieldType[K, H] :: T] = for {
        h <- H.value.apply(row, K.value.name, version)
        t <- T.value(row, version)
      } yield field[K](h) :: t
    }

  implicit def decodeSingleColumn[A](
    implicit
    A: RowDeserializer[A]): RowDecoder[A] = new RowDecoder[A] {
    def apply(row: Row, version: ProtocolVersion): Result[A] =
      A.apply(row, 0, version)
  }
}

trait LowPriorityRowDecoder {

  implicit def decodeCaseClass[A, R <: HList](
    implicit
    gen: LabelledGeneric.Aux[A, R],
    decode: Lazy[RowDecoder[R]]): RowDecoder[A] =
    new RowDecoder[A] {
      def apply(s: Row, version: ProtocolVersion): Result[A] = decode.value(s, version) map (gen from)
    }
}

trait TupleRowDecoder {

  implicit def tuple2RowDecoder[A, B](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B]): RowDecoder[(A, B)] =
    new RowDecoder[(A, B)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version)).tupled
    }

  implicit def tuple3RowDecoder[A, B, C](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C]): RowDecoder[(A, B, C)] =
    new RowDecoder[(A, B, C)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version)).tupled
    }

  implicit def tuple4RowDecoder[A, B, C, D](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D]): RowDecoder[(A, B, C, D)] =
    new RowDecoder[(A, B, C, D)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version)).tupled
    }

  implicit def tuple5RowDecoder[A, B, C, D, E](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E]): RowDecoder[(A, B, C, D, E)] =
    new RowDecoder[(A, B, C, D, E)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version)).tupled
    }

  implicit def tuple6RowDecoder[A, B, C, D, E, F](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F]): RowDecoder[(A, B, C, D, E, F)] =
    new RowDecoder[(A, B, C, D, E, F)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version)).tupled
    }

  implicit def tuple7RowDecoder[A, B, C, D, E, F, G](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G]): RowDecoder[(A, B, C, D, E, F, G)] =
    new RowDecoder[(A, B, C, D, E, F, G)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version)).tupled
    }

  implicit def tuple8RowDecoder[A, B, C, D, E, F, G, H](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H]): RowDecoder[(A, B, C, D, E, F, G, H)] =
    new RowDecoder[(A, B, C, D, E, F, G, H)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version)).tupled
    }

  implicit def tuple9RowDecoder[A, B, C, D, E, F, G, H, I](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I]): RowDecoder[(A, B, C, D, E, F, G, H, I)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version)).tupled
    }

  implicit def tuple10RowDecoder[A, B, C, D, E, F, G, H, I, J](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J]): RowDecoder[(A, B, C, D, E, F, G, H, I, J)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version)).tupled
    }

  implicit def tuple11RowDecoder[A, B, C, D, E, F, G, H, I, J, K](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version)).tupled
    }

  implicit def tuple12RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version)).tupled
    }

  implicit def tuple13RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M)] = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M)] {
    def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M)] =
      (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version)).tupled
  }

  implicit def tuple14RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] {
    def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
      (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version)).tupled
  }

  implicit def tuple15RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version)).tupled
    }

  implicit def tuple16RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O],
    P: RowDeserializer[P]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version) |@| P.apply(row, 15, version)).tupled
    }

  implicit def tuple17RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O],
    P: RowDeserializer[P],
    Q: RowDeserializer[Q]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version) |@| P.apply(row, 15, version) |@| Q.apply(row, 16, version)).tupled
    }

  implicit def tuple18RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O],
    P: RowDeserializer[P],
    Q: RowDeserializer[Q],
    R: RowDeserializer[R]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version) |@| P.apply(row, 15, version) |@| Q.apply(row, 16, version) |@| R.apply(row, 17, version)).tupled
    }

  implicit def tuple19RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O],
    P: RowDeserializer[P],
    Q: RowDeserializer[Q],
    R: RowDeserializer[R],
    S: RowDeserializer[S]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version) |@| P.apply(row, 15, version) |@| Q.apply(row, 16, version) |@| R.apply(row, 17, version) |@| S.apply(row, 18, version)).tupled
    }

  implicit def tuple20RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O],
    P: RowDeserializer[P],
    Q: RowDeserializer[Q],
    R: RowDeserializer[R],
    S: RowDeserializer[S],
    T: RowDeserializer[T]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version) |@| P.apply(row, 15, version) |@| Q.apply(row, 16, version) |@| R.apply(row, 17, version) |@| S.apply(row, 18, version) |@| T.apply(row, 19, version)).tupled
    }

  implicit def tuple21RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O],
    P: RowDeserializer[P],
    Q: RowDeserializer[Q],
    R: RowDeserializer[R],
    S: RowDeserializer[S],
    T: RowDeserializer[T],
    U: RowDeserializer[U]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version) |@| P.apply(row, 15, version) |@| Q.apply(row, 16, version) |@| R.apply(row, 17, version) |@| S.apply(row, 18, version) |@| T.apply(row, 19, version) |@| U.apply(row, 20, version)).tupled
    }

  implicit def tuple22RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
    implicit
    A: RowDeserializer[A],
    B: RowDeserializer[B],
    C: RowDeserializer[C],
    D: RowDeserializer[D],
    E: RowDeserializer[E],
    F: RowDeserializer[F],
    G: RowDeserializer[G],
    H: RowDeserializer[H],
    I: RowDeserializer[I],
    J: RowDeserializer[J],
    K: RowDeserializer[K],
    L: RowDeserializer[L],
    M: RowDeserializer[M],
    N: RowDeserializer[N],
    O: RowDeserializer[O],
    P: RowDeserializer[P],
    Q: RowDeserializer[Q],
    R: RowDeserializer[R],
    S: RowDeserializer[S],
    T: RowDeserializer[T],
    U: RowDeserializer[U],
    V: RowDeserializer[V]): RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
    new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] {
      def apply(row: Row, version: ProtocolVersion): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
        (A.apply(row, 0, version) |@| B.apply(row, 1, version) |@| C.apply(row, 2, version) |@| D.apply(row, 3, version) |@| E.apply(row, 4, version) |@| F.apply(row, 5, version) |@| G.apply(row, 6, version) |@| H.apply(row, 7, version) |@| I.apply(row, 8, version) |@| J.apply(row, 9, version) |@| K.apply(row, 10, version) |@| L.apply(row, 11, version) |@| M.apply(row, 12, version) |@| N.apply(row, 13, version) |@| O.apply(row, 14, version) |@| P.apply(row, 15, version) |@| Q.apply(row, 16, version) |@| R.apply(row, 17, version) |@| S.apply(row, 18, version) |@| T.apply(row, 19, version) |@| U.apply(row, 20, version) |@| V.apply(row, 21, version)).tupled
    }
}
