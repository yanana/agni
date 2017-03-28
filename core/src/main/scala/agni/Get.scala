package agni

import cats.{ Eval, Foldable, MonadError }
import cats.syntax.either._
import cats.syntax.option._
import com.datastax.driver.core.{ ResultSet, Row }

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

trait Get[A] {
  def apply[F[_], E](result: ResultSet)(
    implicit
    F: MonadError[F, E],
    ev: Throwable <:< E
  ): F[A]
}

object Get {
  import syntax._

  def apply[A](implicit A: Get[A]): Get[A] = A

  implicit def getUnit: Get[Unit] = new Get[Unit] {
    override def apply[F[_], E](result: ResultSet)(
      implicit
      F: MonadError[F, E],
      ev: <:<[Throwable, E]
    ): F[Unit] = F.catchNonFatal(())
  }

  implicit def getOneUnsafe[A: RowDecoder]: Get[A] = new Get[A] {
    override def apply[F[_], E](result: ResultSet)(
      implicit
      F: MonadError[F, E],
      ev: <:<[Throwable, E]
    ): F[A] =
      Option(result.one).get.decode.fold(F.raiseError(_), F.pure)
  }

  implicit def getOne[A: RowDecoder]: Get[Option[A]] = new Get[Option[A]] {
    override def apply[F[_], E](result: ResultSet)(
      implicit
      F: MonadError[F, E],
      ev: <:<[Throwable, E]
    ): F[Option[A]] = {
      val row = result.one
      if (row == null) F.pure(none)
      else row.decode.fold(F.raiseError(_), a => F.pure(a.some))
    }
  }

  implicit def getCBF[A: RowDecoder, C[_]](implicit cbf: CanBuildFrom[Nothing, A, C[A]]): Get[C[A]] = new Get[C[A]] {
    override def apply[F[_], E](result: ResultSet)(
      implicit
      F: MonadError[F, E],
      ev: <:<[Throwable, E]
    ): F[C[A]] =
      F.flatMap(F.catchNonFatalEval(Eval.later(result.iterator.asScala)))(to[F, E, A, C])
  }

  private[agni] def to[F[_], E, A: RowDecoder, C[_]](xs: Iterator[Row])(
    implicit
    F: MonadError[F, E],
    ev: Throwable <:< E,
    cbf: CanBuildFrom[Nothing, A, C[A]]
  ): F[C[A]] = {
    val f = Foldable.iteratorFoldM[F, Row, mutable.Builder[A, C[A]]](xs, cbf.apply) {
      case (b, a) => a.decode.fold(F.raiseError(_), a => { b += a; F.pure(b) })
    }
    F.map(f)(_.result())
  }
}
