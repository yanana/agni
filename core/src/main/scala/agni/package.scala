import cats.FlatMap
import cats.data.{ Xor, Kleisli }
import cats.implicits._
import com.datastax.driver.core.Session

import scala.concurrent.{ ExecutionContext, Future }

package object agni {

  type Action[F[_], U] = Kleisli[Lambda[a => F[Throwable Xor a]], Session, U]

  implicit def resultFlatMap(implicit ec: ExecutionContext) = new FlatMap[Lambda[a => Future[Throwable Xor a]]] {
    override def flatMap[A, B](fa: Future[Throwable Xor A])(f: (A) => Future[Throwable Xor B]): Future[Throwable Xor B] = fa.flatMap {
      case x @ Xor.Left(e) => Future(x)
      case Xor.Right(v) => f(v)
    }
    override def map[A, B](fa: Future[Throwable Xor A])(f: (A) => B): Future[Throwable Xor B] = fa.map {
      case x @ Xor.Left(e) => x
      case Xor.Right(v) => f(v).right
    }
  }

}
