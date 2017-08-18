package agni

import cats.MonadError
import com.datastax.driver.core._

trait Agni[F[_], E] { self: GetPreparedStatement =>

  implicit val F: MonadError[F, E]

  protected def ver(session: Session): ProtocolVersion =
    session.getCluster.getConfiguration.getProtocolOptions.getProtocolVersion

  def get[A: Get](query: String)(implicit s: Session, ev: Throwable <:< E): F[A] =
    Get[A].apply[F, E](s.execute(query), ver(s))

  def get[A: Get](stmt: Statement)(implicit s: Session, ev: Throwable <:< E): F[A] =
    Get[A].apply[F, E](s.execute(stmt), ver(s))

  def batchOn: F[BatchStatement] =
    F.pure(new BatchStatement)

  def prepare(q: String)(implicit s: Session, ev: Throwable <:< E): F[PreparedStatement] =
    F.catchNonFatal(getPrepared(s, new SimpleStatement(q)))

  def prepare(stmt: RegularStatement)(implicit s: Session, ev: Throwable <:< E): F[PreparedStatement] =
    F.catchNonFatal(getPrepared(s, stmt))

  def bind[A: Binder](pstmt: PreparedStatement, a: A)(implicit s: Session, ev: Throwable <:< E): F[BoundStatement] =
    Binder[A].apply(pstmt.bind(), ver(s), a).fold(F.raiseError(_), F.pure)
}
