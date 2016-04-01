package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.{ Date, UUID }

import com.datastax.driver.core._
import scodec.bits.{ BitVector, ByteVector }

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

trait ColumnGetter[+T] {
  def apply(row: Row, i: Int): T
}

object ColumnGetter extends LowPriorityColumnGetter {
  def apply[T](f: (Row, Int) => T): ColumnGetter[T] = new ColumnGetter[T] {
    def apply(row: Row, i: Int): T = f(row, i)
  }
}

trait LowPriorityColumnGetter {

  implicit val stringColumnGetter = new ColumnGetter[String] {
    def apply(row: Row, i: Int): String = row.getString(i)
  }

  implicit val intColumnGetter = new ColumnGetter[Int] {
    def apply(row: Row, i: Int): Int = row.getInt(i)
  }

  implicit val longColumnGetter: ColumnGetter[Long] =
    new ColumnGetter[Long] {
      def apply(row: Row, i: Int): Long = row.getLong(i)
    }

  implicit val doubleColumnGetter: ColumnGetter[Double] =
    new ColumnGetter[Double] {
      def apply(row: Row, i: Int): Double = row.getDouble(i)
    }

  implicit val floatColumnGetter: ColumnGetter[Float] =
    new ColumnGetter[Float] {
      def apply(row: Row, i: Int): Float = row.getFloat(i)
    }

  implicit val bigDecimalColumnGetter: ColumnGetter[BigDecimal] =
    new ColumnGetter[BigDecimal] {
      def apply(row: Row, i: Int): BigDecimal = row.getDecimal(i)
    }
  implicit val uuidColumnGetter: ColumnGetter[UUID] =
    new ColumnGetter[UUID] {
      def apply(row: Row, i: Int): UUID = row.getUUID(i)
    }

  implicit val bytesColumnGetter: ColumnGetter[Array[Byte]] =
    new ColumnGetter[Array[Byte]] {
      def apply(row: Row, i: Int): Array[Byte] = row.getBytes(i).array()
    }

  implicit val byteBufferColumnGetter: ColumnGetter[ByteBuffer] =
    new ColumnGetter[ByteBuffer] {
      def apply(row: Row, i: Int): ByteBuffer = row.getBytes(i)
    }

  implicit val byteVectorColumnGetter: ColumnGetter[ByteVector] =
    new ColumnGetter[ByteVector] {
      def apply(row: Row, i: Int): ByteVector = ByteVector(row.getBytes(i))
    }

  implicit val bitVectorColumnGetter: ColumnGetter[BitVector] =
    new ColumnGetter[BitVector] {
      def apply(row: Row, i: Int): BitVector = BitVector(row.getBytes(i))
    }

  implicit val inetColumnGetter: ColumnGetter[InetAddress] =
    new ColumnGetter[InetAddress] {
      def apply(row: Row, i: Int): InetAddress = row.getInet(i)
    }

  implicit val localDateColumnGetter: ColumnGetter[LocalDate] =
    new ColumnGetter[LocalDate] {
      def apply(row: Row, i: Int): LocalDate = row.getDate(i)
    }

  implicit val timestampColumnGetter: ColumnGetter[Date] =
    new ColumnGetter[Date] {
      def apply(row: Row, i: Int): Date = row.getTimestamp(i)
    }

  implicit val tokenColumnGetter: ColumnGetter[Token] =
    new ColumnGetter[Token] {
      def apply(row: Row, i: Int): Token = row.getToken(i)
    }

  implicit val instantColumnGetter: ColumnGetter[Instant] =
    new ColumnGetter[Instant] {
      def apply(row: Row, i: Int): Instant = row.getTimestamp(i).toInstant
    }

  implicit val variantColumnGetter: ColumnGetter[BigInt] =
    new ColumnGetter[BigInt] {
      def apply(row: Row, i: Int): BigInt = row.getVarint(i)
    }

  implicit def seqColumnGetter[A](implicit tag: ClassTag[A]): ColumnGetter[Seq[A]] =
    new ColumnGetter[Seq[A]] {
      def apply(row: Row, i: Int): Seq[A] =
        row.getList(i, classOf[Any]).map(_.asInstanceOf[A])
    }

  implicit def vectorColumnGetter[A](implicit tag: ClassTag[A]): ColumnGetter[Vector[A]] =
    new ColumnGetter[Vector[A]] {
      def apply(row: Row, i: Int): Vector[A] =
        row.getList(i, classOf[Any]).map(_.asInstanceOf[A]).toVector
    }

  implicit def listColumnGetter[A](implicit tag: ClassTag[A]): ColumnGetter[List[A]] =
    new ColumnGetter[List[A]] {
      def apply(row: Row, i: Int): List[A] =
        row.getList(i, classOf[Any]).map(_.asInstanceOf[A]).toList
    }

  implicit def setColumnGetter[A](implicit tag: ClassTag[A]): ColumnGetter[Set[A]] =
    new ColumnGetter[Set[A]] {
      def apply(row: Row, i: Int): Set[A] =
        row.getSet(i, classOf[Any]).map(_.asInstanceOf[A]).toSet
    }

  implicit def mapColumnGetter[K, V](
    implicit
    keyTag: ClassTag[K],
    valTag: ClassTag[V]
  ): ColumnGetter[Map[K, V]] =
    new ColumnGetter[Map[K, V]] {
      def apply(row: Row, i: Int): Map[K, V] = {
        val x = row.getMap(i, classOf[Any], classOf[Any]) map { case (k, v) => (k.asInstanceOf[K], v.asInstanceOf[V]) }
        x.toMap
      }
    }

  implicit val udtValueColumnGetter: ColumnGetter[UDTValue] =
    new ColumnGetter[UDTValue] {
      def apply(row: Row, i: Int): UDTValue = row.getUDTValue(i)
    }

  implicit val tupleValueColumnGetter: ColumnGetter[TupleValue] =
    new ColumnGetter[TupleValue] {
      def apply(row: Row, i: Int): TupleValue = row.getTupleValue(i)
    }
}
