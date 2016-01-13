package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.{ Date, UUID }

import com.datastax.driver.core.{ Row, TupleValue, UDTValue }
import scodec.bits.ByteVector
import shapeless._

import scala.collection.JavaConversions._
import scala.reflect.ClassTag
import scala.util.Try

trait RowDecoder[A] {
  def apply(row: Row, i: Int): A
}

object RowDecoder {

  def apply[A](s: Row)(implicit f: RowDecoder[A]): A = f(s, 0)

  implicit val stringRowDecoder: RowDecoder[String] =
    new RowDecoder[String] {
      def apply(row: Row, i: Int): String = row.getString(i)
    }

  implicit val intRowDecoder: RowDecoder[Int] =
    new RowDecoder[Int] {
      def apply(row: Row, i: Int): Int = row.getInt(i)
    }

  implicit val longRowDecoder: RowDecoder[Long] =
    new RowDecoder[Long] {
      def apply(row: Row, i: Int): Long = row.getLong(i)
    }

  implicit val doubleRowDecoder: RowDecoder[Double] =
    new RowDecoder[Double] {
      def apply(row: Row, i: Int): Double = row.getDouble(i)
    }

  implicit val floatRowDecoder: RowDecoder[Float] =
    new RowDecoder[Float] {
      def apply(row: Row, i: Int): Float = row.getFloat(i)
    }

  implicit val bigDecimalRowDecoder: RowDecoder[BigDecimal] =
    new RowDecoder[BigDecimal] {
      def apply(row: Row, i: Int): BigDecimal = row.getDecimal(i)
    }
  implicit val uuidRowDecoder: RowDecoder[UUID] =
    new RowDecoder[UUID] {
      def apply(row: Row, i: Int): UUID = row.getUUID(i)
    }

  implicit val bytesRowDecoder: RowDecoder[Array[Byte]] =
    new RowDecoder[Array[Byte]] {
      def apply(row: Row, i: Int): Array[Byte] = row.getBytes(i).array()
    }

  implicit val byteBufferRowDecoder: RowDecoder[ByteBuffer] =
    new RowDecoder[ByteBuffer] {
      def apply(row: Row, i: Int): ByteBuffer = row.getBytes(i)
    }

  implicit val byteVectorRowDecoder: RowDecoder[ByteVector] =
    new RowDecoder[ByteVector] {
      def apply(row: Row, i: Int): ByteVector = ByteVector(row.getBytes(i))
    }

  implicit val iNetRowDecoder: RowDecoder[InetAddress] =
    new RowDecoder[InetAddress] {
      def apply(row: Row, i: Int): InetAddress = row.getInet(i)
    }

  implicit val dateRowDecoder: RowDecoder[Date] =
    new RowDecoder[Date] {
      def apply(row: Row, i: Int): Date = row.getDate(i)
    }

  implicit val instantRowDecoder: RowDecoder[Instant] =
    new RowDecoder[Instant] {
      override def apply(row: Row, i: Int): Instant = row.getDate(i).toInstant
    }

  implicit val variantRowDecoder: RowDecoder[BigInt] =
    new RowDecoder[BigInt] {
      def apply(row: Row, i: Int): BigInt = row.getVarint(i)
    }

  implicit def listRowDecoder[A](implicit tag: ClassTag[A]): RowDecoder[Seq[A]] =
    new RowDecoder[Seq[A]] {
      def apply(row: Row, i: Int): Seq[A] =
        row.getList(i, classOf[Any]).map(_.asInstanceOf[A])
    }

  implicit def setRowDecoder[A](implicit tag: ClassTag[A]): RowDecoder[Set[A]] =
    new RowDecoder[Set[A]] {
      def apply(row: Row, i: Int): Set[A] =
        row.getSet(i, classOf[Any]).map(_.asInstanceOf[A]).toSet
    }

  implicit def mapRowDecoder[K, V](
    implicit
    keyTag: ClassTag[K],
    valTag: ClassTag[V]
  ): RowDecoder[Map[K, V]] =
    new RowDecoder[Map[K, V]] {
      def apply(row: Row, i: Int): Map[K, V] = {
        val x = row.getMap(i, classOf[Any], classOf[Any]) map { case (k, v) => (k.asInstanceOf[K], v.asInstanceOf[V]) }
        x.toMap
      }
    }

  implicit val udtRowDecoder: RowDecoder[UDTValue] =
    new RowDecoder[UDTValue] {
      def apply(row: Row, i: Int): UDTValue = row.getUDTValue(i)
    }

  implicit val tupleRowDecoder: RowDecoder[TupleValue] =
    new RowDecoder[TupleValue] {
      def apply(row: Row, i: Int): TupleValue = row.getTupleValue(i)
    }

  implicit def optionRowDecoder[A](
    implicit
    f: Lazy[RowDecoder[A]]
  ): RowDecoder[Option[A]] =
    new RowDecoder[Option[A]] {
      def apply(s: Row, i: Int): Option[A] = s.isNull(i) match {
        case false => Try(f.value(s, i)).toOption.flatMap(x => Option(x))
        case true => None
      }
    }

  implicit val hNilRowDecoder: RowDecoder[HNil] =
    new RowDecoder[HNil] {
      def apply(s: Row, i: Int): HNil = HNil
    }

  implicit def hConsRowDecoder[H: RowDecoder, T <: HList: RowDecoder](
    implicit
    head: Lazy[RowDecoder[H]],
    tail: Lazy[RowDecoder[T]]
  ): RowDecoder[H :: T] = new RowDecoder[H :: T] {
    def apply(s: Row, i: Int): H :: T = head.value(s, i) :: tail.value(s, i + 1)
  }

  implicit def caseClassRowDecoder[A, R <: HList](
    implicit
    gen: Generic.Aux[A, R],
    reprRowDecoder: Lazy[RowDecoder[R]]
  ): RowDecoder[A] = new RowDecoder[A] {
    def apply(s: Row, i: Int): A = gen from reprRowDecoder.value(s, i)
  }

}
