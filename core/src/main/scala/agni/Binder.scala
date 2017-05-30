package agni

import cats.syntax.either._
import com.datastax.driver.core.{ BoundStatement, ProtocolVersion }
import shapeless.{ ::, HList, HNil, LabelledGeneric, Lazy, Witness }
import shapeless.labelled.FieldType

trait Binder[A] {
  def apply(bound: BoundStatement, version: ProtocolVersion, a: A): Result[BoundStatement]
}

object Binder extends LowPriorityBinder with TupleBind {

  def apply[A](implicit A: Binder[A]) = A

  implicit val bindHnil: Binder[HNil] = new Binder[HNil] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, a: HNil): Result[BoundStatement] = Right(bound)
  }

  implicit def bindLabelledHList[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: Lazy[RowSerializer[H]],
    T: Lazy[Binder[T]]
  ): Binder[FieldType[K, H] :: T] =
    new Binder[FieldType[K, H] :: T] {
      override def apply(bound: BoundStatement, version: ProtocolVersion, xs: FieldType[K, H] :: T): Result[BoundStatement] =
        xs match {
          case h :: t => for {
            _ <- H.value.apply(bound, K.value.name, h, version)
            r <- T.value.apply(bound, version, t)
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

trait TupleBind {

  implicit def bindTuple2[A, B](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B]
  ) = new Binder[(A, B)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
    } yield bound
  }

  implicit def bindTuple3[A, B, C](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C]
  ) = new Binder[(A, B, C)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
    } yield bound
  }
  implicit def bindTuple4[A, B, C, D](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D]
  ) = new Binder[(A, B, C, D)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
    } yield bound
  }
  implicit def bindTuple5[A, B, C, D, E](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E]
  ) = new Binder[(A, B, C, D, E)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
    } yield bound
  }
  implicit def bindTuple6[A, B, C, D, E, F](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F]
  ) = new Binder[(A, B, C, D, E, F)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
    } yield bound
  }
  implicit def bindTuple7[A, B, C, D, E, F, G](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G]
  ) = new Binder[(A, B, C, D, E, F, G)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
    } yield bound
  }
  implicit def bindTuple8[A, B, C, D, E, F, G, H](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H]
  ) = new Binder[(A, B, C, D, E, F, G, H)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
    } yield bound
  }
  implicit def bindTuple9[A, B, C, D, E, F, G, H, I](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I]
  ) = new Binder[(A, B, C, D, E, F, G, H, I)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 3, xs._3, version)
      _ <- D.apply(bound, 2, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
    } yield bound
  }
  implicit def bindTuple10[A, B, C, D, E, F, G, H, I, J](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
    } yield bound
  }
  implicit def bindTuple11[A, B, C, D, E, F, G, H, I, J, K](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
    } yield bound
  }
  implicit def bindTuple12[A, B, C, D, E, F, G, H, I, J, K, L](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 11, xs._11, version)
      _ <- L.apply(bound, 12, xs._12, version)
    } yield bound
  }
  implicit def bindTuple13[A, B, C, D, E, F, G, H, I, J, K, L, M](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
    } yield bound
  }
  implicit def bindTuple14[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
    } yield bound
  }
  implicit def bindTuple15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
    } yield bound
  }
  implicit def bindTuple16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O],
    P: RowSerializer[P]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
      _ <- P.apply(bound, 15, xs._16, version)
    } yield bound
  }
  implicit def bindTuple17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O],
    P: RowSerializer[P],
    Q: RowSerializer[Q]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
      _ <- P.apply(bound, 15, xs._16, version)
      _ <- Q.apply(bound, 16, xs._17, version)
    } yield bound
  }
  implicit def bindTuple18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O],
    P: RowSerializer[P],
    Q: RowSerializer[Q],
    R: RowSerializer[R]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
      _ <- P.apply(bound, 15, xs._16, version)
      _ <- Q.apply(bound, 16, xs._17, version)
      _ <- R.apply(bound, 17, xs._18, version)
    } yield bound
  }
  implicit def bindTuple19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O],
    P: RowSerializer[P],
    Q: RowSerializer[Q],
    R: RowSerializer[R],
    S: RowSerializer[S]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
      _ <- P.apply(bound, 15, xs._16, version)
      _ <- Q.apply(bound, 16, xs._17, version)
      _ <- R.apply(bound, 17, xs._18, version)
      _ <- S.apply(bound, 18, xs._19, version)
    } yield bound
  }
  implicit def bindTuple20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O],
    P: RowSerializer[P],
    Q: RowSerializer[Q],
    R: RowSerializer[R],
    S: RowSerializer[S],
    T: RowSerializer[T]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
      _ <- P.apply(bound, 15, xs._16, version)
      _ <- Q.apply(bound, 16, xs._17, version)
      _ <- R.apply(bound, 17, xs._18, version)
      _ <- S.apply(bound, 18, xs._19, version)
      _ <- T.apply(bound, 19, xs._20, version)
    } yield bound
  }
  implicit def bindTuple21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O],
    P: RowSerializer[P],
    Q: RowSerializer[Q],
    R: RowSerializer[R],
    S: RowSerializer[S],
    T: RowSerializer[T],
    U: RowSerializer[U]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
      _ <- P.apply(bound, 15, xs._16, version)
      _ <- Q.apply(bound, 16, xs._17, version)
      _ <- R.apply(bound, 17, xs._18, version)
      _ <- S.apply(bound, 18, xs._19, version)
      _ <- T.apply(bound, 19, xs._20, version)
      _ <- U.apply(bound, 20, xs._21, version)
    } yield bound
  }

  implicit def bindTuple22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
    implicit
    A: RowSerializer[A],
    B: RowSerializer[B],
    C: RowSerializer[C],
    D: RowSerializer[D],
    E: RowSerializer[E],
    F: RowSerializer[F],
    G: RowSerializer[G],
    H: RowSerializer[H],
    I: RowSerializer[I],
    J: RowSerializer[J],
    K: RowSerializer[K],
    L: RowSerializer[L],
    M: RowSerializer[M],
    N: RowSerializer[N],
    O: RowSerializer[O],
    P: RowSerializer[P],
    Q: RowSerializer[Q],
    R: RowSerializer[R],
    S: RowSerializer[S],
    T: RowSerializer[T],
    U: RowSerializer[U],
    V: RowSerializer[V]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] {
    override def apply(bound: BoundStatement, version: ProtocolVersion, xs: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)): Result[BoundStatement] = for {
      _ <- A.apply(bound, 0, xs._1, version)
      _ <- B.apply(bound, 1, xs._2, version)
      _ <- C.apply(bound, 2, xs._3, version)
      _ <- D.apply(bound, 3, xs._4, version)
      _ <- E.apply(bound, 4, xs._5, version)
      _ <- F.apply(bound, 5, xs._6, version)
      _ <- G.apply(bound, 6, xs._7, version)
      _ <- H.apply(bound, 7, xs._8, version)
      _ <- I.apply(bound, 8, xs._9, version)
      _ <- J.apply(bound, 9, xs._10, version)
      _ <- K.apply(bound, 10, xs._11, version)
      _ <- L.apply(bound, 11, xs._12, version)
      _ <- M.apply(bound, 12, xs._13, version)
      _ <- N.apply(bound, 13, xs._14, version)
      _ <- O.apply(bound, 14, xs._15, version)
      _ <- P.apply(bound, 15, xs._16, version)
      _ <- Q.apply(bound, 16, xs._17, version)
      _ <- R.apply(bound, 17, xs._18, version)
      _ <- S.apply(bound, 18, xs._19, version)
      _ <- T.apply(bound, 19, xs._20, version)
      _ <- U.apply(bound, 20, xs._21, version)
      _ <- V.apply(bound, 21, xs._22, version)
    } yield bound
  }
}
