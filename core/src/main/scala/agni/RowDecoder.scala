package agni

import cats.instances.either._
import cats.syntax.either._
import cats.syntax.cartesian._
import com.datastax.driver.core.Row
import shapeless._, labelled._

trait RowDecoder[A] {
  def apply(row: Row): Result[A]
}

object RowDecoder extends LowPriorityRowDecoder with TupleRowDecoder {

  def apply[A](implicit A: RowDecoder[A]): RowDecoder[A] = A

  def unsafeGet[A: RowDecoder](f: Row => Result[A]): RowDecoder[A] =
    new RowDecoder[A] {
      def apply(s: Row): Result[A] = f(s)
    }

  implicit val hNilRowDecoder: RowDecoder[HNil] =
    new RowDecoder[HNil] {
      def apply(s: Row): Result[HNil] = Right(HNil)
    }

  implicit def labelledHListDecoder[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: Lazy[NamedColumnGetter[H]],
    T: Lazy[RowDecoder[T]]
  ): RowDecoder[FieldType[K, H] :: T] =
    new RowDecoder[FieldType[K, H] :: T] {
      def apply(row: Row): Result[FieldType[K, H] :: T] = for {
        h <- H.value(row, K.value.name)
        t <- T.value(row)
      } yield field[K](h) :: t
    }
}

trait LowPriorityRowDecoder {

  implicit def caseClassRowDecoder[A, R <: HList](
    implicit
    gen: LabelledGeneric.Aux[A, R],
    decode: Lazy[RowDecoder[R]]
  ): RowDecoder[A] =
    new RowDecoder[A] {
      def apply(s: Row): Result[A] = decode.value(s) map (gen from)
    }
}

trait TupleRowDecoder {

  implicit def aRowDecoder[A](
    implicit
    A: IndexedColumnGetter[A]
  ) = new RowDecoder[A] {
    def apply(row: Row): Result[A] = A(row, 0)
  }

  implicit def tuple2RowDecoder[A, B](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B]
  ) = new RowDecoder[(A, B)] {
    def apply(row: Row): Result[(A, B)] =
      (A(row, 0) |@| B(row, 1)).tupled
  }

  implicit def tuple3RowDecoder[A, B, C](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C]
  ) = new RowDecoder[(A, B, C)] {
    def apply(row: Row): Result[(A, B, C)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2)).tupled
  }

  implicit def tuple4RowDecoder[A, B, C, D](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D]
  ) = new RowDecoder[(A, B, C, D)] {
    def apply(row: Row): Result[(A, B, C, D)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3)).tupled
  }

  implicit def tuple5RowDecoder[A, B, C, D, E](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E]
  ) = new RowDecoder[(A, B, C, D, E)] {
    def apply(row: Row): Result[(A, B, C, D, E)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4)).tupled
  }

  implicit def tuple6RowDecoder[A, B, C, D, E, F](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F]
  ) = new RowDecoder[(A, B, C, D, E, F)] {
    def apply(row: Row): Result[(A, B, C, D, E, F)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5)).tupled
  }

  implicit def tuple7RowDecoder[A, B, C, D, E, F, G](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G]
  ) = new RowDecoder[(A, B, C, D, E, F, G)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6)).tupled
  }

  implicit def tuple8RowDecoder[A, B, C, D, E, F, G, H](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7)).tupled
  }

  implicit def tuple9RowDecoder[A, B, C, D, E, F, G, H, I](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8)).tupled
  }

  implicit def tuple10RowDecoder[A, B, C, D, E, F, G, H, I, J](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9)).tupled
  }

  implicit def tuple11RowDecoder[A, B, C, D, E, F, G, H, I, J, K](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10)).tupled
  }

  implicit def tuple12RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11)).tupled
  }

  implicit def tuple13RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12)).tupled
  }

  implicit def tuple14RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13)).tupled
  }

  implicit def tuple15RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14)).tupled
  }

  implicit def tuple16RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O],
    P: IndexedColumnGetter[P]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14) |@| P(row, 15)).tupled
  }

  implicit def tuple17RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O],
    P: IndexedColumnGetter[P],
    Q: IndexedColumnGetter[Q]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14) |@| P(row, 15) |@| Q(row, 16)).tupled
  }

  implicit def tuple18RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O],
    P: IndexedColumnGetter[P],
    Q: IndexedColumnGetter[Q],
    R: IndexedColumnGetter[R]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14) |@| P(row, 15) |@| Q(row, 16) |@| R(row, 17)).tupled
  }

  implicit def tuple19RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O],
    P: IndexedColumnGetter[P],
    Q: IndexedColumnGetter[Q],
    R: IndexedColumnGetter[R],
    S: IndexedColumnGetter[S]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14) |@| P(row, 15) |@| Q(row, 16) |@| R(row, 17) |@| S(row, 18)).tupled
  }

  implicit def tuple20RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O],
    P: IndexedColumnGetter[P],
    Q: IndexedColumnGetter[Q],
    R: IndexedColumnGetter[R],
    S: IndexedColumnGetter[S],
    T: IndexedColumnGetter[T]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14) |@| P(row, 15) |@| Q(row, 16) |@| R(row, 17) |@| S(row, 18) |@| T(row, 19)).tupled
  }

  implicit def tuple21RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O],
    P: IndexedColumnGetter[P],
    Q: IndexedColumnGetter[Q],
    R: IndexedColumnGetter[R],
    S: IndexedColumnGetter[S],
    T: IndexedColumnGetter[T],
    U: IndexedColumnGetter[U]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14) |@| P(row, 15) |@| Q(row, 16) |@| R(row, 17) |@| S(row, 18) |@| T(row, 19) |@| U(row, 20)).tupled
  }

  implicit def tuple22RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
    implicit
    A: IndexedColumnGetter[A],
    B: IndexedColumnGetter[B],
    C: IndexedColumnGetter[C],
    D: IndexedColumnGetter[D],
    E: IndexedColumnGetter[E],
    F: IndexedColumnGetter[F],
    G: IndexedColumnGetter[G],
    H: IndexedColumnGetter[H],
    I: IndexedColumnGetter[I],
    J: IndexedColumnGetter[J],
    K: IndexedColumnGetter[K],
    L: IndexedColumnGetter[L],
    M: IndexedColumnGetter[M],
    N: IndexedColumnGetter[N],
    O: IndexedColumnGetter[O],
    P: IndexedColumnGetter[P],
    Q: IndexedColumnGetter[Q],
    R: IndexedColumnGetter[R],
    S: IndexedColumnGetter[S],
    T: IndexedColumnGetter[T],
    U: IndexedColumnGetter[U],
    V: IndexedColumnGetter[V]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] {
    def apply(row: Row): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
      (A(row, 0) |@| B(row, 1) |@| C(row, 2) |@| D(row, 3) |@| E(row, 4) |@| F(row, 5) |@| G(row, 6) |@| H(row, 7) |@| I(row, 8) |@| J(row, 9) |@| K(row, 10) |@| L(row, 11) |@| M(row, 12) |@| N(row, 13) |@| O(row, 14) |@| P(row, 15) |@| Q(row, 16) |@| R(row, 17) |@| S(row, 18) |@| T(row, 19) |@| U(row, 20) |@| V(row, 21)).tupled
  }
}
