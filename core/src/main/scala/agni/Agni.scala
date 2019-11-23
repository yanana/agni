package agni

import cats.MonadError
import com.datastax.oss.driver.api.core.cql.{ BoundStatement, PreparedStatement, SimpleStatement }
import com.datastax.oss.driver.api.core.{ CqlSession, ProtocolVersion }

import scala.collection.JavaConverters._

trait Agni[F[_], E] {

  implicit val F: MonadError[F, E]

  protected def ver(session: CqlSession): ProtocolVersion =
    session.getContext().getProtocolVersion()

  def get[A: Get](query: String)(implicit s: CqlSession, ev: Throwable <:< E): F[A] =
    Get[A].apply[F, E](s.execute(query).all().asScala, ver(s))

  def get[A: Get](stmt: BoundStatement)(implicit s: CqlSession, ev: Throwable <:< E): F[A] =
    Get[A].apply[F, E](s.execute(stmt).all().asScala, ver(s))

  def prepare(q: String)(implicit s: CqlSession, ev: Throwable <:< E): F[PreparedStatement] =
    F.catchNonFatal(s.prepare(q))

  def prepare(stmt: SimpleStatement)(implicit s: CqlSession, ev: Throwable <:< E): F[PreparedStatement] =
    F.catchNonFatal(s.prepare(stmt))

  def bind[A: Binder](pstmt: PreparedStatement, a: A)(implicit s: CqlSession, ev: Throwable <:< E): F[BoundStatement] =
    Binder[A].apply(pstmt.bind(), ver(s), a).fold(F.raiseError(_), F.pure)
}
