package agni

import com.datastax.driver.core.Row
import shapeless._, labelled._

import scala.util.Try

trait RowDecoder[A] {
  def apply(row: Row, i: Int): A
}

object RowDecoder extends LowPriorityRowDecoder with TupleRowDecoder {

  def apply[A](implicit A: RowDecoder[A]): RowDecoder[A] = A

  def unsafeGet[A](f: (Row, Int) => A)(implicit A: RowDecoder[A]): RowDecoder[A] =
    new RowDecoder[A] {
      def apply(s: Row, i: Int) = f(s, i)
    }

  implicit def optionRowDecoder[A](
    implicit
    f: Lazy[ColumnGetter[A]]
  ): RowDecoder[Option[A]] =
    new RowDecoder[Option[A]] {
      def apply(s: Row, i: Int): Option[A] = s.isNull(i) match {
        case false => Try(f.value(s, i)).toOption.flatMap(x => Option(x))
        case true => None
      }
    }

  implicit def rowDecoder[A](
    implicit
    f: Lazy[ColumnGetter[A]]
  ): RowDecoder[A] =
    new RowDecoder[A] {
      def apply(s: Row, i: Int): A = f.value(s, i)
    }

  implicit def recordDecoder[K <: Symbol, H, T <: HList](
    implicit
    H: Lazy[RowDecoder[H]],
    T: Lazy[RowDecoder[T]]
  ): RowDecoder[FieldType[K, H] :: T] =
    new RowDecoder[FieldType[K, H] :: T] {
      def apply(row: Row, i: Int): FieldType[K, H] :: T =
        field[K](H.value(row, i)) :: T.value(row, i + 1)
    }

}

trait LowPriorityRowDecoder {
  implicit val hNilRowDecoder: RowDecoder[HNil] =
    new RowDecoder[HNil] {
      def apply(s: Row, i: Int): HNil = HNil
    }

  implicit def hConsRowDecoder[H: RowDecoder, T <: HList: RowDecoder](
    implicit
    head: Lazy[RowDecoder[H]],
    tail: Lazy[RowDecoder[T]]
  ): RowDecoder[H :: T] = new RowDecoder[H :: T] {
    def apply(s: Row, i: Int): H :: T = head.value(s, i) :: tail.value(s, i + 1)
  }

  implicit def caseClassRowDecoder[A, R <: HList](
    implicit
    gen: Generic.Aux[A, R],
    reprRowDecoder: Lazy[RowDecoder[R]]
  ): RowDecoder[A] = new RowDecoder[A] {
    def apply(s: Row, i: Int): A = gen from reprRowDecoder.value(s, i)
  }

}

trait TupleRowDecoder {

  implicit def tuple2RowDecoder[A, B](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B]
  ) = new RowDecoder[(A, B)] {
    def apply(row: Row, i: Int): (A, B) =
      (A(row, 0), B(row, 1))
  }

  implicit def tuple3RowDecoder[A, B, C](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C]
  ) = new RowDecoder[(A, B, C)] {
    def apply(row: Row, i: Int): (A, B, C) =
      (A(row, 0), B(row, 1), C(row, 2))
  }

  implicit def tuple4RowDecoder[A, B, C, D](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D]
  ) = new RowDecoder[(A, B, C, D)] {
    def apply(row: Row, i: Int): (A, B, C, D) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3))
  }

  implicit def tuple5RowDecoder[A, B, C, D, E](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E]
  ) = new RowDecoder[(A, B, C, D, E)] {
    def apply(row: Row, i: Int): (A, B, C, D, E) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4))
  }

  implicit def tuple6RowDecoder[A, B, C, D, E, F](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F]
  ) = new RowDecoder[(A, B, C, D, E, F)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5))
  }

  implicit def tuple7RowDecoder[A, B, C, D, E, F, G](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G]
  ) = new RowDecoder[(A, B, C, D, E, F, G)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6))
  }

  implicit def tuple8RowDecoder[A, B, C, D, E, F, G, H](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7))
  }

  implicit def tuple9RowDecoder[A, B, C, D, E, F, G, H, I](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8))
  }

  implicit def tuple10RowDecoder[A, B, C, D, E, F, G, H, I, J](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9))
  }

  implicit def tuple11RowDecoder[A, B, C, D, E, F, G, H, I, J, K](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10))
  }

  implicit def tuple12RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11))
  }

  implicit def tuple13RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12))
  }

  implicit def tuple14RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13))
  }

  implicit def tuple15RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14))
  }

  implicit def tuple16RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O],
    P: RowDecoder[P]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14), P(row, 15))
  }

  implicit def tuple17RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O],
    P: RowDecoder[P],
    Q: RowDecoder[Q]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14), P(row, 15), Q(row, 16))
  }

  implicit def tuple18RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O],
    P: RowDecoder[P],
    Q: RowDecoder[Q],
    R: RowDecoder[R]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14), P(row, 15), Q(row, 16), R(row, 17))
  }

  implicit def tuple19RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O],
    P: RowDecoder[P],
    Q: RowDecoder[Q],
    R: RowDecoder[R],
    S: RowDecoder[S]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14), P(row, 15), Q(row, 16), R(row, 17), S(row, 18))
  }

  implicit def tuple20RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O],
    P: RowDecoder[P],
    Q: RowDecoder[Q],
    R: RowDecoder[R],
    S: RowDecoder[S],
    T: RowDecoder[T]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14), P(row, 15), Q(row, 16), R(row, 17), S(row, 18), T(row, 19))
  }

  implicit def tuple21RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O],
    P: RowDecoder[P],
    Q: RowDecoder[Q],
    R: RowDecoder[R],
    S: RowDecoder[S],
    T: RowDecoder[T],
    U: RowDecoder[U]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14), P(row, 15), Q(row, 16), R(row, 17), S(row, 18), T(row, 19), U(row, 20))
  }

  implicit def tuple22RowDecoder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
    implicit
    A: RowDecoder[A],
    B: RowDecoder[B],
    C: RowDecoder[C],
    D: RowDecoder[D],
    E: RowDecoder[E],
    F: RowDecoder[F],
    G: RowDecoder[G],
    H: RowDecoder[H],
    I: RowDecoder[I],
    J: RowDecoder[J],
    K: RowDecoder[K],
    L: RowDecoder[L],
    M: RowDecoder[M],
    N: RowDecoder[N],
    O: RowDecoder[O],
    P: RowDecoder[P],
    Q: RowDecoder[Q],
    R: RowDecoder[R],
    S: RowDecoder[S],
    T: RowDecoder[T],
    U: RowDecoder[U],
    V: RowDecoder[V]
  ) = new RowDecoder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] {
    def apply(row: Row, i: Int): (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) =
      (A(row, 0), B(row, 1), C(row, 2), D(row, 3), E(row, 4), F(row, 5), G(row, 6), H(row, 7), I(row, 8), J(row, 9), K(row, 10), L(row, 11), M(row, 12), N(row, 13), O(row, 14), P(row, 15), Q(row, 16), R(row, 17), S(row, 18), T(row, 19), U(row, 20), V(row, 21))
  }
}
