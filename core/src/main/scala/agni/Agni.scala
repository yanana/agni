package agni

import cats.data.Kleisli
import cats.MonadError
import com.datastax.driver.core._

trait Agni[F[_], E] extends Functions[F] { self: GetPreparedStatement =>

  implicit val F: MonadError[F, E]

  protected def ver(session: Session): ProtocolVersion =
    session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersion

  def lift[A](a: F[A]): Action[A] =
    Kleisli.lift[F, Session, A](a)

  def pure[A](a: A): Action[A] =
    Kleisli.pure[F, Session, A](a)

  def get[A](query: String)(implicit A: Get[A], ev: Throwable <:< E): Action[A] =
    withSession[A](session => A.apply[F, E](session.execute(query), ver(session)))

  def get[A](stmt: Statement)(implicit A: Get[A], ev: Throwable <:< E): Action[A] =
    withSession[A](session => A.apply[F, E](session.execute(stmt), ver(session)))

  val batchOn: Action[BatchStatement] =
    Kleisli.pure[F, Session, BatchStatement](new BatchStatement)

  def prepare(q: String)(implicit ev: Throwable <:< E): Action[PreparedStatement] =
    withSession(session => F.catchNonFatal(getPrepared(session, new SimpleStatement(q))))

  def prepare(stmt: RegularStatement)(implicit ev: Throwable <:< E): Action[PreparedStatement] =
    withSession(session => F.catchNonFatal(getPrepared(session, stmt)))

  def bind[A](pstmt: PreparedStatement, a: A)(implicit A: Binder[A], ev: Throwable <:< E): Action[BoundStatement] =
    withSession(session => A.apply(pstmt.bind(), ver(session), a).fold(F.raiseError(_), F.pure))
}
