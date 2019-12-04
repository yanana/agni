package agni
package util

import java.util.concurrent.{ CompletionStage, Executor }
import java.util.function.BiConsumer

import cats.instances.either._
import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.cql.AsyncResultSet

import scala.collection.JavaConverters._

object Futures {

  def async[A: Get](
    rsf: CompletionStage[AsyncResultSet],
    cb: Either[Throwable, A] => Unit,
    ex: Executor
  ): ProtocolVersion => Unit = pVer =>
    rsf.whenCompleteAsync(new BiConsumer[AsyncResultSet, Throwable] {
      def accept(u: AsyncResultSet, t: Throwable): Unit =
        if (t != null)
          cb(t.asLeft)
        else
          cb(Get[A].apply[Either[Throwable, ?], Throwable](u.currentPage().asScala, pVer))
    }, ex)
}
