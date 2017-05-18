package agni

import cats.data.Kleisli
import cats.MonadError
import com.datastax.driver.core._

import scala.collection.JavaConverters._

trait Agni[F[_], E] extends Functions[F] { self: GetPreparedStatement =>
  import syntax._

  implicit val F: MonadError[F, E]

  @Deprecated
  def lift[A](a: A): Action[A] =
    Kleisli.pure[F, Session, A](a)

  @Deprecated
  def execute[A: RowDecoder](query: String)(implicit ev: Throwable <:< E): Action[Iterator[Result[A]]] =
    execute(new SimpleStatement(query))

  @Deprecated
  def execute[A: RowDecoder](stmt: Statement)(implicit ev: Throwable <:< E): Action[Iterator[Result[A]]] =
    withSession(session => F.catchNonFatal(session.execute(stmt).iterator.asScala.map(_.decode)))

  def pure[A](a: A): Action[A] =
    Kleisli.pure[F, Session, A](a)

  def get[A](query: String)(implicit A: Get[A], ev: Throwable <:< E): Action[A] =
    withSession[A](s => A.apply[F, E](s.execute(query)))

  def get[A](stmt: Statement)(implicit A: Get[A], ev: Throwable <:< E): Action[A] =
    withSession[A](s => A.apply[F, E](s.execute(stmt)))

  val batchOn: Action[BatchStatement] =
    Kleisli.pure[F, Session, BatchStatement](new BatchStatement)

  def prepare(q: String)(implicit ev: Throwable <:< E): Action[PreparedStatement] =
    withSession(session => F.catchNonFatal(getPrepared(session, new SimpleStatement(q))))

  def prepare(stmt: RegularStatement)(implicit ev: Throwable <:< E): Action[PreparedStatement] =
    withSession(session => F.catchNonFatal(getPrepared(session, stmt)))

  def bind[A](bstmt: BatchStatement, pstmt: PreparedStatement, a: A)(implicit A: Binder[A]): Action[Unit] =
    Kleisli.pure(bstmt.add(A(pstmt, a)))

  def bind[A](pstmt: PreparedStatement, a: A)(implicit A: Binder[A]): Action[BoundStatement] =
    Kleisli.pure(A(pstmt, a))

  def bind[A](a: A)(implicit A: Binder[A]): Kleisli[F, PreparedStatement, BoundStatement] =
    Kleisli[F, PreparedStatement, BoundStatement](pstmt => F.pure(A(pstmt, a)))

  val bind: Kleisli[F, PreparedStatement, BoundStatement] =
    Kleisli[F, PreparedStatement, BoundStatement](pstmt => F.pure(pstmt.bind()))
}
