package agni

import cats.{ Eval, MonadError }
import com.datastax.driver.core.{ BatchStatement, BoundStatement, PreparedStatement, Statement }

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

abstract class Agni[F[_], E](
    implicit
    F: MonadError[F, E],
    ev: Throwable <:< E
) extends Functions[F] {
  import Function.const
  import syntax._

  // TODO: configurable cache
  val queryCache: TrieMap[String, PreparedStatement] = TrieMap.empty

  def lift[A](a: A): Action[A] =
    withSession[A](const(F.pure(a)))

  def execute[A: RowDecoder](query: String): Action[Iterator[A]] =
    withSession { session =>
      F.catchNonFatalEval(Eval.later(session.execute(query).iterator.asScala.map(_.decode)))
    }

  def execute[A: RowDecoder](stmt: Statement): Action[Iterator[A]] =
    withSession { session =>
      F.catchNonFatalEval(Eval.later(session.execute(stmt).iterator.asScala.map(_.decode)))
    }

  val batchOn: Action[BatchStatement] =
    withSession(const(F.pure(new BatchStatement)))

  def prepare(query: String): Action[PreparedStatement] =
    withSession { session =>
      F.catchNonFatalEval(Eval.later(queryCache.getOrElseUpdate(query, session.prepare(query))))
    }

  def bind[A](bstmt: BatchStatement, pstmt: PreparedStatement, a: A)(
    implicit
    A: Binder[A]
  ): Action[Unit] =
    withSession(const(F.pure(bstmt.add(A(pstmt, a)))))

  def bind[A](pstmt: PreparedStatement, a: A)(
    implicit
    A: Binder[A]
  ): Action[BoundStatement] =
    withSession(const(F.catchNonFatal(A(pstmt, a))))
}
