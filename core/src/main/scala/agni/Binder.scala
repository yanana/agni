package agni

import com.datastax.driver.core.{ BoundStatement, PreparedStatement }
import shapeless._
import shapeless.ops.hlist._

trait Binder[A] {
  def apply(pstmt: PreparedStatement, a: A): BoundStatement
}

object Binder extends LowPriorityBinder with TupleBinder {
  def apply[A](implicit A: Binder[A]) = A
}

trait LowPriorityBinder {

  implicit val hnilBinder: Binder[HNil] = new Binder[HNil] {
    override def apply(pstmt: PreparedStatement, a: HNil): BoundStatement = pstmt.bind()
  }

  implicit def hlistBinder[H <: HList, O <: HList](
    implicit
    mapperAux: Mapper.Aux[scalaToJava.type, H, O],
    toTraversableAux: ToTraversable.Aux[O, List, Object]
  ): Binder[H] = new Binder[H] {
    override def apply(pstmt: PreparedStatement, a: H): BoundStatement =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def ccBinder[A, H <: HList, O <: HList](
    implicit
    gen: Generic.Aux[A, H],
    mapperAux: Mapper.Aux[scalaToJava.type, H, O],
    toTraversableAux: ToTraversable.Aux[O, List, Object]
  ): Binder[A] = new Binder[A] {
    override def apply(pstmt: PreparedStatement, a: A): BoundStatement = {
      pstmt.bind(gen.to(a).map(scalaToJava).toList[Object]: _*)
    }
  }
}

trait TupleBinder {
  import shapeless.ops.tuple.{ ToTraversable => _, _ }
  import shapeless.syntax.std.tuple._

  implicit def tuple2Binder[A, B, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B)] {
    def apply(pstmt: PreparedStatement, a: (A, B)): BoundStatement =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple3Binder[A, B, C, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple4Binder[A, B, C, D, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple5Binder[A, B, C, D, E, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple6Binder[A, B, C, D, E, F, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple7Binder[A, B, C, D, E, F, G, O <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G), scalaToJava.type, O],
    toTraversableAux: ToTraversable.Aux[O, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple8Binder[A, B, C, D, E, F, G, H, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple9Binder[A, B, C, D, E, F, G, H, I, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple10Binder[A, B, C, D, E, F, G, H, I, J, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple11Binder[A, B, C, D, E, F, G, H, I, J, K, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple12Binder[A, B, C, D, E, F, G, H, I, J, K, L, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple13Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple14Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple15Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple16Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple17Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple18Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple19Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple20Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple21Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }

  implicit def tuple22Binder[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, OL <: HList](
    implicit
    mapperAux: Mapper.Aux[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V), scalaToJava.type, OL],
    toTraversableAux: ToTraversable.Aux[OL, List, Object]
  ) = new Binder[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] {
    def apply(pstmt: PreparedStatement, a: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)) =
      pstmt.bind(a.map(scalaToJava).toList[Object]: _*)
  }
}
