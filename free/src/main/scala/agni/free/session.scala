package agni.free

import agni.{ Async, Binder, Get }
import cats.data.Kleisli
import cats.free.Free
import cats.{ InjectK, ~> }
import com.datastax.oss.driver.api.core.cql.{ BoundStatement, SimpleStatement, PreparedStatement }
import com.datastax.oss.driver.api.core.CqlSession

object session {

  trait Api[F[_]] {

    type SessionF[A] = Free[F, A]

    def prepare(stmt: SimpleStatement): SessionF[PreparedStatement]

    def bind[A: Binder](stmt: PreparedStatement, a: A): SessionF[BoundStatement]

    def execute[A: Get](stmt: BoundStatement): SessionF[A]

    def executeAsync[A: Get](stmt: BoundStatement): SessionF[A]
  }

  type AsyncF[F[_]] = Async[F, Throwable]

  trait SessionOp[A] {
    def run[F[_]](implicit F: AsyncF[F]): Kleisli[F, CqlSession, A]
  }

  type SessionIO[A] = Free[SessionOp, A]

  object SessionOp {
    trait Handler[F[_], J] extends (SessionOp ~> Kleisli[F, J, ?])

    object Handler extends LowPriorityHandler {

      implicit def sessionOpHandler[F[_]](implicit F: AsyncF[F]): Handler[F, CqlSession] =
        new Handler[F, CqlSession] {
          def apply[A](fa: SessionOp[A]): Kleisli[F, CqlSession, A] = fa.run[F]
        }
    }

    trait LowPriorityHandler {

      implicit def sessionOpHandlerWithJ[F[_], J](implicit F: AsyncF[F], fj: J => CqlSession): Handler[F, J] =
        new Handler[F, J] {
          def apply[A](fa: SessionOp[A]): Kleisli[F, J, A] = fa.run[F].local(fj)
        }
    }

    final case class Prepare(stmt: SimpleStatement) extends SessionOp[PreparedStatement] {
      def run[F[_]](implicit F: AsyncF[F]): Kleisli[F, CqlSession, PreparedStatement] =
        Kleisli(implicit s => F.prepare(stmt))
    }

    final case class Bind[A: Binder](stmt: PreparedStatement, a: A) extends SessionOp[BoundStatement] {
      def run[F[_]](implicit F: AsyncF[F]): Kleisli[F, CqlSession, BoundStatement] =
        Kleisli(implicit s => F.bind(stmt, a))
    }

    final case class Execute[A: Get](stmt: BoundStatement) extends SessionOp[A] {
      def run[F[_]](implicit F: AsyncF[F]): Kleisli[F, CqlSession, A] =
        Kleisli(implicit s => F.get(stmt))
    }

    final case class ExecuteAsync[A: Get](stmt: BoundStatement) extends SessionOp[A] {
      def run[F[_]](implicit F: AsyncF[F]): Kleisli[F, CqlSession, A] =
        Kleisli(implicit s => F.getAsync(stmt))
    }
  }

  private[session] abstract class Ops[F[_]](implicit I: InjectK[SessionOp, F]) extends Api[F] {
    import SessionOp._

    def prepare(stmt: SimpleStatement): SessionF[PreparedStatement] =
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

  object iota {
    import _root_.iota._

    final class SessionOps[F[A] <: CopK[_, A]](implicit I: CopK.Inject[SessionOp, F]) extends Ops[F]

    object SessionOps {
      implicit def sessionOpsForIota[F[A] <: CopK[_, A]](implicit I: CopK.Inject[SessionOp, F]): SessionOps[F] = new SessionOps
    }
  }
}
