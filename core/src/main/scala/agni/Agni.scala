package agni

import cats.{ Eval, MonadError }
import com.datastax.driver.core._
import shapeless._, ops.hlist._

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

abstract class Agni[F[_], E] extends Functions[F] {
  import Function.const
  import syntax._

  // TODO: configurable cache
  val queryCache: TrieMap[String, PreparedStatement] = TrieMap.empty

  def lift[A](a: A)(implicit MF: MonadError[F, E]): Action[A] =
    withSession[A](const(MF.pureEval(Eval.later(a))))

  def execute[A: RowDecoder](query: String)(implicit MF: MonadError[F, E]): Action[Iterator[A]] =
    withSession { session =>
      MF.pureEval(Eval.later(session.execute(query).iterator.asScala.map(_.decode(0))))
    }

  def execute[A: RowDecoder](stmt: Statement)(implicit MF: MonadError[F, E]): Action[Iterator[A]] =
    withSession { session =>
      MF.pureEval(Eval.later(session.execute(stmt).iterator.asScala.map(_.decode(0))))
    }

  def batchOn(implicit MF: MonadError[F, E]): Action[BatchStatement] =
    withSession(const(MF.pure(new BatchStatement)))

  def prepare(query: String)(implicit MF: MonadError[F, E]): Action[PreparedStatement] =
    withSession { session =>
      MF.pureEval(Eval.later(queryCache.getOrElseUpdate(query, session.prepare(query))))
    }

  def bind[A](bstmt: BatchStatement, pstmt: PreparedStatement, a: A)(
    implicit
    B: Binder[A],
    MF: MonadError[F, E]
  ): Action[Unit] =
    withSession(const(MF.pureEval(Eval.later(bstmt.add(B(pstmt, a))))))

  def bind[A](pstmt: PreparedStatement, a: A)(
    implicit
    B: Binder[A],
    MF: MonadError[F, E]
  ): Action[BoundStatement] =
    withSession(const(MF.pureEval(Eval.later(B(pstmt, a)))))
}
