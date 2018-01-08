package agni.free

import agni.{ Binder, Get, GetPreparedStatement }
import cats.data.Kleisli
import cats.free.Free
import cats.{ InjectK, ~> }
import com.datastax.driver.core._

object session {

  trait Api[F[_]] {

    type SessionF[A] = Free[F, A]

    def prepare(stmt: RegularStatement): SessionF[PreparedStatement]

    def bind[A: Binder](stmt: PreparedStatement, a: A): SessionF[BoundStatement]

    def execute[A: Get](stmt: BoundStatement): SessionF[A]

    def executeAsync[A: Get](stmt: BoundStatement): SessionF[A]
  }

  type Cacheable[F[_]] = agni.Async[F, Throwable] with GetPreparedStatement

  trait SessionOp[A] {
    def run[F[_]](implicit F: Cacheable[F]): Kleisli[F, Session, A]
  }

  type SessionIO[A] = Free[SessionOp, A]

  object SessionOp {
    trait Handler[F[_], J] extends (SessionOp ~> Kleisli[F, J, ?])

    object Handler extends LowPriorityHandler {

      implicit def sessionOpHandler[F[_]](implicit F: Cacheable[F]): Handler[F, Session] =
        new Handler[F, Session] {
          def apply[A](fa: SessionOp[A]): Kleisli[F, Session, A] = fa.run[F]
        }
    }

    trait LowPriorityHandler {

      implicit def sessionOpHandlerWithJ[F[_], J](implicit F: Cacheable[F], fj: J => Session): Handler[F, J] =
        new Handler[F, J] {
          def apply[A](fa: SessionOp[A]): Kleisli[F, J, A] = fa.run[F].local(fj)
        }
    }

    final case class Prepare(stmt: RegularStatement) extends SessionOp[PreparedStatement] {
      def run[F[_]](implicit F: Cacheable[F]): Kleisli[F, Session, PreparedStatement] =
        Kleisli(implicit s => F.prepare(stmt))
    }

    final case class Bind[A: Binder](stmt: PreparedStatement, a: A) extends SessionOp[BoundStatement] {
      def run[F[_]](implicit F: Cacheable[F]): Kleisli[F, Session, BoundStatement] =
        Kleisli(implicit s => F.bind(stmt, a))
    }

    final case class Execute[A: Get](stmt: BoundStatement) extends SessionOp[A] {
      def run[F[_]](implicit F: Cacheable[F]): Kleisli[F, Session, A] =
        Kleisli(implicit s => F.get(stmt))
    }

    final case class ExecuteAsync[A: Get](stmt: BoundStatement) extends SessionOp[A] {
      def run[F[_]](implicit F: Cacheable[F]): Kleisli[F, Session, A] =
        Kleisli(implicit s => F.getAsync(stmt))
    }
  }

  private[session] abstract class Ops[F[_]](implicit I: InjectK[SessionOp, F]) extends Api[F] {
    import SessionOp._

    def prepare(stmt: RegularStatement): SessionF[PreparedStatement] =
      Free.inject[SessionOp, F](Prepare(stmt))

    def bind[A: Binder](stmt: PreparedStatement, a: A): SessionF[BoundStatement] =
      Free.inject[SessionOp, F](Bind(stmt, a))

    def execute[A: Get](stmt: BoundStatement): SessionF[A] =
      Free.inject[SessionOp, F](Execute(stmt))

    def executeAsync[A: Get](stmt: BoundStatement): SessionF[A] =
      Free.inject[SessionOp, F](ExecuteAsync(stmt))
  }

  final class SessionOps[F[_]](implicit I: InjectK[SessionOp, F]) extends Ops[F]

  object SessionOps {
    implicit def sessionOps[F[_]](implicit I: InjectK[SessionOp, F]): SessionOps[F] = new SessionOps()
  }
}
