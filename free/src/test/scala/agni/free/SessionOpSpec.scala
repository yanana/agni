package agni.free

import agni.{ Binder, Get }
import cats.data.Kleisli
import cats.free.Free
import cats.~>
import org.scalatest.FunSpec
import org.scalatestplus.mockito.MockitoSugar
import com.datastax.oss.driver.api.core.cql.{ BoundStatement, PreparedStatement, SimpleStatement }
import com.datastax.oss.driver.api.core.CqlSession

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class SessionOpSpec extends FunSpec with MockitoSugar {
  import session._
  import cats.instances.future._

  case class Env(session: CqlSession)

  lazy val env: Env = Env(mock[CqlSession])

  type G[A] = Kleisli[Future, Env, A]

  implicit lazy val getSession: Env => CqlSession = _.session

  implicit val asyncF: AsyncF[Future] = new agni.std.Future {

    override def prepare(stmt: SimpleStatement)(
      implicit
      s: CqlSession,
      ev: Throwable <:< Throwable
    ): Future[PreparedStatement] = Future.successful(mock[PreparedStatement])

    override def bind[A: Binder](stmt: PreparedStatement, a: A)(
      implicit
      s: CqlSession,
      ev: Throwable <:< Throwable
    ): Future[BoundStatement] = Future.successful(mock[BoundStatement])

    override def get[A: Get](stmt: BoundStatement)(
      implicit
      s: CqlSession,
      ev: Throwable <:< Throwable
    ): Future[A] = Future.successful(().asInstanceOf[A])

    override def getAsync[A: Get](stmt: BoundStatement)(
      implicit
      s: CqlSession
    ): Future[A] = Future.successful(().asInstanceOf[A])
  }

  implicit val handler: SessionOp ~> G =
    SessionOp.Handler.sessionOpHandlerWithJ[Future, Env]

  lazy val S: SessionOps[SessionOp] = implicitly

  def interpret(program: Free[SessionOp, Unit]): G[Unit] =
    program.foldMap(handler)

  def run(program: Free[SessionOp, Unit]): Future[Unit] =
    interpret(program).run(env)

  describe("SessionOp") {
    it("should run the program normaly") {

      def program: Free[SessionOp, Unit] = for {
        p <- S.prepare(SimpleStatement.newInstance("USE ?;"))
        b <- S.bind(p, "1")
        _ <- S.execute[Unit](b)
        _ <- S.executeAsync[Unit](b)
      } yield ()

      Await.result(run(program), Duration.Inf)
    }
  }
}
