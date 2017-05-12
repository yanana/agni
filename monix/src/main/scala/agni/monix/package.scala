package agni

import cats.syntax.either._
import cats.{ Eval, MonadError }
import _root_.monix.eval.{ Task => MTask }

package object monix {

  object cats {
    implicit val taskToMonadError: MonadError[MTask, Throwable] =
      new MonadError[MTask, Throwable] {
        override def flatMap[A, B](fa: MTask[A])(f: (A) => MTask[B]): MTask[B] =
          fa.flatMap(f)

        override def tailRecM[A, B](a: A)(f: (A) => MTask[Either[A, B]]): MTask[B] =
          f(a).flatMap(_.fold(tailRecM(_)(f), MTask.pure))

        override def product[A, B](fa: MTask[A], fb: MTask[B]): MTask[(A, B)] =
          MTask.mapBoth(fa, fb)((_, _))

        override def raiseError[A](e: Throwable): MTask[A] =
          MTask.raiseError(e)

        override def handleErrorWith[A](fa: MTask[A])(f: Throwable => MTask[A]): MTask[A] =
          fa.onErrorHandleWith(f)

        override def pure[A](a: A): MTask[A] =
          MTask.pure(a)

        override def catchNonFatal[A](a: => A)(implicit ev: Throwable <:< Throwable): MTask[A] =
          MTask(a)

        override def catchNonFatalEval[A](a: Eval[A])(implicit ev: Throwable <:< Throwable): MTask[A] =
          MTask(a.value)
      }
  }

}
