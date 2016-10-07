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

trait IndexedColumnGetter[+T] {
  def apply(row: Row, i: Int): T
}

object IndexedColumnGetter extends LowPriorityIndexedColumnGetter {
  def apply[T](implicit T: IndexedColumnGetter[T]): IndexedColumnGetter[T] = T

  implicit val stringColumnGetter = new IndexedColumnGetter[String] {
    def apply(row: Row, i: Int): String = row.getString(i)
  }

  implicit val intColumnGetter = new IndexedColumnGetter[Int] {
    def apply(row: Row, i: Int): Int = row.getInt(i)
  }

  implicit val longColumnGetter: IndexedColumnGetter[Long] =
    new IndexedColumnGetter[Long] {
      def apply(row: Row, i: Int): Long = row.getLong(i)
    }

  implicit val doubleColumnGetter: IndexedColumnGetter[Double] =
    new IndexedColumnGetter[Double] {
      def apply(row: Row, i: Int): Double = row.getDouble(i)
    }

  implicit val floatColumnGetter: IndexedColumnGetter[Float] =
    new IndexedColumnGetter[Float] {
      def apply(row: Row, i: Int): Float = row.getFloat(i)
    }

  implicit val bigDecimalColumnGetter: IndexedColumnGetter[BigDecimal] =
    new IndexedColumnGetter[BigDecimal] {
      def apply(row: Row, i: Int): BigDecimal = row.getDecimal(i)
    }

  implicit val uuidColumnGetter: IndexedColumnGetter[UUID] =
    new IndexedColumnGetter[UUID] {
      def apply(row: Row, i: Int): UUID = row.getUUID(i)
    }

  implicit val bytesColumnGetter: IndexedColumnGetter[Array[Byte]] =
    new IndexedColumnGetter[Array[Byte]] {
      def apply(row: Row, i: Int): Array[Byte] = row.getBytes(i).array()
    }

  implicit val byteBufferColumnGetter: IndexedColumnGetter[ByteBuffer] =
    new IndexedColumnGetter[ByteBuffer] {
      def apply(row: Row, i: Int): ByteBuffer = row.getBytes(i)
    }

  implicit val byteVectorColumnGetter: IndexedColumnGetter[ByteVector] =
    new IndexedColumnGetter[ByteVector] {
      def apply(row: Row, i: Int): ByteVector = ByteVector(row.getBytes(i))
    }

  implicit val bitVectorColumnGetter: IndexedColumnGetter[BitVector] =
    new IndexedColumnGetter[BitVector] {
      def apply(row: Row, i: Int): BitVector = BitVector(row.getBytes(i))
    }

  implicit val inetColumnGetter: IndexedColumnGetter[InetAddress] =
    new IndexedColumnGetter[InetAddress] {
      def apply(row: Row, i: Int): InetAddress = row.getInet(i)
    }

  implicit val localDateColumnGetter: IndexedColumnGetter[LocalDate] =
    new IndexedColumnGetter[LocalDate] {
      def apply(row: Row, i: Int): LocalDate = row.getDate(i)
    }

  implicit val timestampColumnGetter: IndexedColumnGetter[Date] =
    new IndexedColumnGetter[Date] {
      def apply(row: Row, i: Int): Date = row.getTimestamp(i)
    }

  implicit val tokenColumnGetter: IndexedColumnGetter[Token] =
    new IndexedColumnGetter[Token] {
      def apply(row: Row, i: Int): Token = row.getToken(i)
    }

  implicit val instantColumnGetter: IndexedColumnGetter[Instant] =
    new IndexedColumnGetter[Instant] {
      def apply(row: Row, i: Int): Instant = row.getTimestamp(i).toInstant
    }

  implicit val varintColumnGetter: IndexedColumnGetter[BigInt] =
    new IndexedColumnGetter[BigInt] {
      def apply(row: Row, i: Int): BigInt = row.getVarint(i)
    }

  implicit val udtValueColumnGetter: IndexedColumnGetter[UDTValue] =
    new IndexedColumnGetter[UDTValue] {
      def apply(row: Row, i: Int): UDTValue = row.getUDTValue(i)
    }

  implicit val tupleValueColumnGetter: IndexedColumnGetter[TupleValue] =
    new IndexedColumnGetter[TupleValue] {
      def apply(row: Row, i: Int): TupleValue = row.getTupleValue(i)
    }

  def mapColumnGetter[K, V, V0](f: V => V0)(
    implicit
    keyTag: ClassTag[K],
    valTag: ClassTag[V]
  ): IndexedColumnGetter[Map[K, V0]] =
    new IndexedColumnGetter[Map[K, V0]] {
      def apply(row: Row, i: Int): Map[K, V0] =
        row.getMap(
          i,
          TypeToken.of[K](keyTag.runtimeClass.asInstanceOf[Class[K]]),
          TypeToken.of[V](keyTag.runtimeClass.asInstanceOf[Class[V]])
        ).toMap.mapValues(f)
    }

  implicit val setS: IndexedColumnGetter[Set[String]] = setColumnGetter[String, String](identity)
  implicit val setI: IndexedColumnGetter[Set[Int]] = setColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val setL: IndexedColumnGetter[Set[Long]] = setColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val setD: IndexedColumnGetter[Set[Double]] = setColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val setF: IndexedColumnGetter[Set[Float]] = setColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val listS: IndexedColumnGetter[List[String]] = listColumnGetter[String, String](identity)
  implicit val listI: IndexedColumnGetter[List[Int]] = listColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val listL: IndexedColumnGetter[List[Long]] = listColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val listD: IndexedColumnGetter[List[Double]] = listColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val listF: IndexedColumnGetter[List[Float]] = listColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val vecS: IndexedColumnGetter[Vector[String]] = vectorColumnGetter[String, String](identity)
  implicit val vecI: IndexedColumnGetter[Vector[Int]] = vectorColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val vecL: IndexedColumnGetter[Vector[Long]] = vectorColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val vecD: IndexedColumnGetter[Vector[Double]] = vectorColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val vecF: IndexedColumnGetter[Vector[Float]] = vectorColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val streamS: IndexedColumnGetter[Stream[String]] = streamColumnGetter[String, String](identity)
  implicit val streamI: IndexedColumnGetter[Stream[Int]] = streamColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val streamL: IndexedColumnGetter[Stream[Long]] = streamColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val streamD: IndexedColumnGetter[Stream[Double]] = streamColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val streamF: IndexedColumnGetter[Stream[Float]] = streamColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val mapSS: IndexedColumnGetter[Map[String, String]] = mapColumnGetter[String, String, String](identity)
  implicit val mapSI: IndexedColumnGetter[Map[String, Int]] = mapColumnGetter[String, java.lang.Integer, Int](_.toInt)
  implicit val mapSL: IndexedColumnGetter[Map[String, Long]] = mapColumnGetter[String, java.lang.Long, Long](_.toLong)
  implicit val mapSD: IndexedColumnGetter[Map[String, Double]] = mapColumnGetter[String, java.lang.Double, Double](_.toDouble)
  implicit val mapSF: IndexedColumnGetter[Map[String, Float]] = mapColumnGetter[String, java.lang.Float, Float](_.toFloat)
}

trait LowPriorityIndexedColumnGetter {

  implicit def optionColumnGetter[A](implicit A: IndexedColumnGetter[A]) = new IndexedColumnGetter[Option[A]] {
    def apply(row: Row, i: Int): Option[A] =
      if (row.isNull(i)) None else Option(A(row, i))
  }

  def streamColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Stream[A0]] =
    new IndexedColumnGetter[Stream[A0]] {
      def apply(row: Row, i: Int): Stream[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toStream.map(f)
    }

  def seqColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Seq[A0]] =
    new IndexedColumnGetter[Seq[A0]] {
      def apply(row: Row, i: Int): Seq[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).map(_.asInstanceOf[A0])
    }

  def vectorColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Vector[A0]] =
    new IndexedColumnGetter[Vector[A0]] {
      def apply(row: Row, i: Int): Vector[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toVector.map(_.asInstanceOf[A0])
    }

  def listColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[List[A0]] =
    new IndexedColumnGetter[List[A0]] {
      def apply(row: Row, i: Int): List[A0] =
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toList.map(f)
    }

  def setColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Set[A0]] =
    new IndexedColumnGetter[Set[A0]] {
      def apply(row: Row, i: Int): Set[A0] = {
        val x: Set[A] = row.getSet(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toSet
        x.map(f)
      }
    }
}
