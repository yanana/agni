package agni

import cats.data.Kleisli
import cats.{ Eval, MonadError }
import com.datastax.driver.core._

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

abstract class Agni[F[_], E](
    implicit
    F: MonadError[F, E],
    ev: Throwable <:< E
) extends Functions[F] {
  import syntax._

  // TODO: configurable cache
  val queryCache: TrieMap[String, PreparedStatement] = TrieMap.empty

  @Deprecated
  def lift[A](a: A): Action[A] =
    Kleisli.pure[F, Session, A](a)

  @Deprecated
  def execute[A: RowDecoder](query: String): Action[Iterator[Result[A]]] =
    execute(new SimpleStatement(query))

  @Deprecated
  def execute[A: RowDecoder](stmt: Statement): Action[Iterator[Result[A]]] =
    withSession(session => F.catchNonFatal(session.execute(stmt).iterator.asScala.map(_.decode)))

  def pure[A](a: A): Action[A] =
    Kleisli.pure[F, Session, A](a)

  def get[A](query: String)(implicit A: Get[A]): Action[A] =
    withSession[A](s => A.apply[F, E](s.execute(query)))

  def get[A](stmt: Statement)(implicit A: Get[A]): Action[A] =
    withSession[A](s => A.apply[F, E](s.execute(stmt)))

  val batchOn: Action[BatchStatement] =
    Kleisli.pure[F, Session, BatchStatement](new BatchStatement)

  def prepare(query: String): Action[PreparedStatement] =
    withSession(session => F.catchNonFatalEval(Eval.later(queryCache.getOrElseUpdate(query, session.prepare(query)))))

  def prepare(stmt: RegularStatement): Action[PreparedStatement] =
    withSession(session => F.catchNonFatalEval(Eval.later(queryCache.getOrElseUpdate(stmt.toString, session.prepare(stmt)))))

  def bind[A](bstmt: BatchStatement, pstmt: PreparedStatement, a: A)(implicit A: Binder[A]): Action[Unit] =
    Kleisli.pure(bstmt.add(A(pstmt, a)))

  def bind[A](pstmt: PreparedStatement, a: A)(implicit A: Binder[A]): Action[BoundStatement] =
    Kleisli.pure(A(pstmt, a))
}
