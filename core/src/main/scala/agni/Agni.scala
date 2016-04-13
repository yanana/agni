package agni

import cats.{ Eval, MonadError }
import com.datastax.driver.core._
import scodec.bits.{ BitVector, ByteVector }

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

trait Agni[F[_], E] extends Functions[F] {

  // TODO: configurable cache
  val queryCache: TrieMap[String, PreparedStatement] = TrieMap.empty

  def lift[A](a: A)(implicit MF: MonadError[F, E]): Action[A] =
    withSession[A](_ => MF.pure(a))

  def execute[A](query: String)(implicit decoder: RowDecoder[A], MF: MonadError[F, E]): Action[Iterator[A]] =
    withSession { session =>
      MF.pureEval(
        Eval.now(
          session.execute(query).iterator.asScala.map(decoder(_, 0))
        )
      )
    }

  def execute[A](stmt: Statement)(implicit decoder: RowDecoder[A], MF: MonadError[F, E]): Action[Iterator[A]] =
    withSession { session =>
      MF.pureEval(Eval.now(session.execute(stmt).iterator.asScala.map(decoder(_, 0))))
    }

  def batchOn(implicit MF: MonadError[F, E]): Action[BatchStatement] =
    withSession { _ =>
      MF.pure(new BatchStatement)
    }

  def prepare(query: String)(implicit MF: MonadError[F, E]): Action[PreparedStatement] =
    withSession { session =>
      MF.pureEval(
        Eval.now(
          queryCache.getOrElseUpdate(query, session.prepare(query))
        )
      )
    }

  def bind(bstmt: BatchStatement, pstmt: PreparedStatement, ps: Any*)(implicit MF: MonadError[F, E]): Action[Unit] =
    withSession { session =>
      MF.pureEval(
        Eval.now(
          bstmt.add(pstmt.bind(ps.map(convertToJava): _*))
        )
      )
    }

  def bind(pstmt: PreparedStatement, ps: Any*)(implicit MF: MonadError[F, E]): Action[BoundStatement] =
    withSession { session =>
      MF.pureEval(
        Eval.now(
          pstmt.bind(ps.map(convertToJava): _*)
        )
      )
    }

  // TODO: improve implementation
  val convertToJava: Any => Object = {
    case a: Set[Any] => a.map(convertToJava).asJava: java.util.Set[Object]
    case a: Seq[Any] => a.map(convertToJava).asJava: java.util.List[Object]
    case a: Map[Any, Any] => a.map { case (k, v) => (convertToJava(k), convertToJava(v)) }.asJava: java.util.Map[Object, Object]
    case a: String => a
    case a: java.nio.ByteBuffer => a
    case a: Long => a: java.lang.Long
    case a: Int => a: java.lang.Integer
    case a: Float => a: java.lang.Float
    case a: Double => a: java.lang.Double
    case a: BigDecimal => a.bigDecimal
    case a: BigInt => a.bigInteger
    case a: ByteVector => a.toArray
    case a: BitVector => a.toByteArray
    case Some(a) => convertToJava(a)
    case None => null
    case a: Object => a
    case a => new RuntimeException(s"uncaught class type $a")
  }

}
