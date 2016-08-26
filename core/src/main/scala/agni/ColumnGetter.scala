package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.{ Date, UUID }

import com.datastax.driver.core._
import com.google.common.reflect.TypeToken
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

  implicit val varintColumnGetter: ColumnGetter[BigInt] =
    new ColumnGetter[BigInt] {
      def apply(row: Row, i: Int): BigInt = row.getVarint(i)
    }

  implicit val udtValueColumnGetter: ColumnGetter[UDTValue] =
    new ColumnGetter[UDTValue] {
      def apply(row: Row, i: Int): UDTValue = row.getUDTValue(i)
    }

  implicit val tupleValueColumnGetter: ColumnGetter[TupleValue] =
    new ColumnGetter[TupleValue] {
      def apply(row: Row, i: Int): TupleValue = row.getTupleValue(i)
    }

  def streamColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): ColumnGetter[Stream[A0]] =
    new ColumnGetter[Stream[A0]] {
      def apply(row: Row, i: Int): Stream[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toStream.map(f)
    }

  def seqColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): ColumnGetter[Seq[A0]] =
    new ColumnGetter[Seq[A0]] {
      def apply(row: Row, i: Int): Seq[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).map(_.asInstanceOf[A0])
    }

  def vectorColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): ColumnGetter[Vector[A0]] =
    new ColumnGetter[Vector[A0]] {
      def apply(row: Row, i: Int): Vector[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toVector.map(_.asInstanceOf[A0])
    }

  def listColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): ColumnGetter[List[A0]] =
    new ColumnGetter[List[A0]] {
      def apply(row: Row, i: Int): List[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toList.map(f)
    }

  def setColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): ColumnGetter[Set[A0]] =
    new ColumnGetter[Set[A0]] {
      def apply(row: Row, i: Int): Set[A0] = {
        val x: Set[A] = row.getSet(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toSet
        x.map(f)
      }
    }

  def mapColumnGetter[K, V, V0](f: V => V0)(
    implicit
    keyTag: ClassTag[K],
    valTag: ClassTag[V]
  ): ColumnGetter[Map[K, V0]] =
    new ColumnGetter[Map[K, V0]] {
      def apply(row: Row, i: Int): Map[K, V0] =
        row.getMap(
          i,
          TypeToken.of[K](keyTag.runtimeClass.asInstanceOf[Class[K]]),
          TypeToken.of[V](keyTag.runtimeClass.asInstanceOf[Class[V]])
        ).toMap.mapValues(f)
    }

  implicit val setS = setColumnGetter[String, String](identity)
  implicit val setI = setColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val setL = setColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val setD = setColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val setF = setColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val listS = listColumnGetter[String, String](identity)
  implicit val listI = listColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val listL = listColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val listD = listColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val listF = listColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val vecS = vectorColumnGetter[String, String](identity)
  implicit val vecI = vectorColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val vecL = vectorColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val vecD = vectorColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val vecF = vectorColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val streamS = streamColumnGetter[String, String](identity)
  implicit val streamI = streamColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val streamL = streamColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val streamD = streamColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val streamF = streamColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val mapSS = mapColumnGetter[String, String, String](identity)
  implicit val mapSI = mapColumnGetter[String, java.lang.Integer, Int](_.toInt)
  implicit val mapSL = mapColumnGetter[String, java.lang.Long, Long](_.toLong)
  implicit val mapSD = mapColumnGetter[String, java.lang.Double, Double](_.toDouble)
  implicit val mapSF = mapColumnGetter[String, java.lang.Float, Float](_.toFloat)
}
