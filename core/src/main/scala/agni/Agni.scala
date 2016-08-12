package agni

import cats.{ Eval, MonadError }
import com.datastax.driver.core._
import shapeless._, ops.hlist._

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

abstract class Agni[F[_], E] extends Functions[F] {
  import Function.const

  // TODO: configurable cache
  val queryCache: TrieMap[String, PreparedStatement] = TrieMap.empty

  def lift[A](a: A)(implicit MF: MonadError[F, E]): Action[A] =
    withSession[A](const(MF.pureEval(Eval.later(a))))

  def execute[A](query: String)(implicit decoder: RowDecoder[A], MF: MonadError[F, E]): Action[Iterator[A]] =
    withSession { session =>
      MF.pureEval(
        Eval.later(
          session.execute(query).iterator.asScala.map(decoder(_, 0))
        )
      )
    }

  def execute[A](stmt: Statement)(implicit decoder: RowDecoder[A], MF: MonadError[F, E]): Action[Iterator[A]] =
    withSession { session =>
      MF.pureEval(Eval.later(session.execute(stmt).iterator.asScala.map(decoder(_, 0))))
    }

  def batchOn(implicit MF: MonadError[F, E]): Action[BatchStatement] =
    withSession(const(MF.pure(new BatchStatement)))

  def prepare(query: String)(implicit MF: MonadError[F, E]): Action[PreparedStatement] =
    withSession { session =>
      MF.pureEval(
        Eval.later(
          queryCache.getOrElseUpdate(query, session.prepare(query))
        )
      )
    }

  def bind[H <: HList, O <: HList](bstmt: BatchStatement, pstmt: PreparedStatement, ps: H)(
    implicit
    mapperAux: Mapper.Aux[scalaToJava.type, H, O],
    toTraversableAux: ToTraversable.Aux[O, List, Object],
    MF: MonadError[F, E]
  ): Action[Unit] =
    withSession(const(MF.pureEval(Eval.later(bstmt.add(pstmt.bind(ps.map(scalaToJava).toList[Object]: _*))))))

  def bind[H <: HList, O <: HList](pstmt: PreparedStatement, ps: H)(
    implicit
    mapperAux: Mapper.Aux[scalaToJava.type, H, O],
    toTraversableAux: ToTraversable.Aux[O, List, Object],
    MF: MonadError[F, E]
  ): Action[BoundStatement] =
    withSession(const(MF.pureEval(Eval.later(pstmt.bind(ps.map(scalaToJava).toList[Object]: _*)))))
}
