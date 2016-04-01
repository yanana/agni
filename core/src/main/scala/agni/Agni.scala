package agni

import com.datastax.driver.core.{ PreparedStatement, Statement, BatchStatement }
import scodec.bits.{ BitVector, ByteVector }

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }

object Agni extends Functions {
  // TODO: configurable cache
  val queryCache: TrieMap[String, PreparedStatement] = TrieMap.empty

  def lift[A](a: A)(
    implicit
    ctx: ExecutionContext
  ): Action[Future, A] =
    withSession[Future, A] { session =>
      Future(a)
    }

  def execute[A](query: String)(
    implicit
    decoder: RowDecoder[A],
    ctx: ExecutionContext
  ): Action[Future, Iterator[A]] =
    withSession[Future, Iterator[A]] { session =>
      Future(session.execute(query).iterator.asScala.map(x => decoder(x, 0)))
    }

  def execute[A](stmt: Statement)(
    implicit
    decoder: RowDecoder[A],
    ctx: ExecutionContext
  ): Action[Future, Iterator[A]] =
    withSession[Future, Iterator[A]] { session =>
      Future(session.execute(stmt).iterator.asScala.map(x => decoder(x, 0)))
    }

  def batchOn(
    implicit
    ctx: ExecutionContext
  ): Action[Future, BatchStatement] =
    withSession[Future, BatchStatement] { _ =>
      Future(new BatchStatement)
    }

  def prepare(query: String)(
    implicit
    ctx: ExecutionContext
  ): Action[Future, PreparedStatement] =
    withSession[Future, PreparedStatement] { session =>
      Future(queryCache.getOrElseUpdate(query, session.prepare(query)))
    }

  def bind(bstmt: BatchStatement, pstmt: PreparedStatement, ps: Any*)(
    implicit
    ctx: ExecutionContext
  ): Action[Future, Unit] =
    withSession[Future, Unit] { session =>
      Future(bstmt.add(pstmt.bind(ps.map(convertToJava): _*)))
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
