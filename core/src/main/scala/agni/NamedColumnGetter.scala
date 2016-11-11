package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.{ Date, UUID }

import com.datastax.driver.core._
import com.google.common.reflect.TypeToken

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

trait NamedColumnGetter[+T] {
  def apply(row: Row, name: String): T
}

object NamedColumnGetter extends LowPriorityNamedColumnGetter {

  def apply[T](implicit T: NamedColumnGetter[T]): NamedColumnGetter[T] = T

  implicit val stringColumnGetter = new NamedColumnGetter[String] {
    def apply(row: Row, name: String): String = row.getString(name)
  }

  implicit val intColumnGetter = new NamedColumnGetter[Int] {
    def apply(row: Row, name: String): Int = row.getInt(name)
  }

  implicit val longColumnGetter: NamedColumnGetter[Long] =
    new NamedColumnGetter[Long] {
      def apply(row: Row, name: String): Long = row.getLong(name)
    }

  implicit val doubleColumnGetter: NamedColumnGetter[Double] =
    new NamedColumnGetter[Double] {
      def apply(row: Row, name: String): Double = row.getDouble(name)
    }

  implicit val floatColumnGetter: NamedColumnGetter[Float] =
    new NamedColumnGetter[Float] {
      def apply(row: Row, name: String): Float = row.getFloat(name)
    }

  implicit val bigDecimalColumnGetter: NamedColumnGetter[BigDecimal] =
    new NamedColumnGetter[BigDecimal] {
      def apply(row: Row, name: String): BigDecimal = row.getDecimal(name)
    }

  implicit val uuidColumnGetter: NamedColumnGetter[UUID] =
    new NamedColumnGetter[UUID] {
      def apply(row: Row, name: String): UUID = row.getUUID(name)
    }

  implicit val bytesColumnGetter: NamedColumnGetter[Array[Byte]] =
    new NamedColumnGetter[Array[Byte]] {
      def apply(row: Row, name: String): Array[Byte] = row.getBytes(name).array()
    }

  implicit val byteBufferColumnGetter: NamedColumnGetter[ByteBuffer] =
    new NamedColumnGetter[ByteBuffer] {
      def apply(row: Row, name: String): ByteBuffer = row.getBytes(name)
    }

  implicit val inetColumnGetter: NamedColumnGetter[InetAddress] =
    new NamedColumnGetter[InetAddress] {
      def apply(row: Row, name: String): InetAddress = row.getInet(name)
    }

  implicit val localDateColumnGetter: NamedColumnGetter[LocalDate] =
    new NamedColumnGetter[LocalDate] {
      def apply(row: Row, name: String): LocalDate = row.getDate(name)
    }

  implicit val timestampColumnGetter: NamedColumnGetter[Date] =
    new NamedColumnGetter[Date] {
      def apply(row: Row, name: String): Date = row.getTimestamp(name)
    }

  implicit val tokenColumnGetter: NamedColumnGetter[Token] =
    new NamedColumnGetter[Token] {
      def apply(row: Row, name: String): Token = row.getToken(name)
    }

  implicit val instantColumnGetter: NamedColumnGetter[Instant] =
    new NamedColumnGetter[Instant] {
      def apply(row: Row, name: String): Instant = row.getTimestamp(name).toInstant
    }

  implicit val varintColumnGetter: NamedColumnGetter[BigInt] =
    new NamedColumnGetter[BigInt] {
      def apply(row: Row, name: String): BigInt = row.getVarint(name)
    }

  implicit val udtValueColumnGetter: NamedColumnGetter[UDTValue] =
    new NamedColumnGetter[UDTValue] {
      def apply(row: Row, name: String): UDTValue = row.getUDTValue(name)
    }

  implicit val tupleValueColumnGetter: NamedColumnGetter[TupleValue] =
    new NamedColumnGetter[TupleValue] {
      def apply(row: Row, name: String): TupleValue = row.getTupleValue(name)
    }

  implicit val setS: NamedColumnGetter[Set[String]] = setColumnGetter[String, String](identity)
  implicit val setI: NamedColumnGetter[Set[Int]] = setColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val setL: NamedColumnGetter[Set[Long]] = setColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val setD: NamedColumnGetter[Set[Double]] = setColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val setF: NamedColumnGetter[Set[Float]] = setColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val listS: NamedColumnGetter[List[String]] = listColumnGetter[String, String](identity)
  implicit val listI: NamedColumnGetter[List[Int]] = listColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val listL: NamedColumnGetter[List[Long]] = listColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val listD: NamedColumnGetter[List[Double]] = listColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val listF: NamedColumnGetter[List[Float]] = listColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val vecS: NamedColumnGetter[Vector[String]] = vectorColumnGetter[String, String](identity)
  implicit val vecI: NamedColumnGetter[Vector[Int]] = vectorColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val vecL: NamedColumnGetter[Vector[Long]] = vectorColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val vecD: NamedColumnGetter[Vector[Double]] = vectorColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val vecF: NamedColumnGetter[Vector[Float]] = vectorColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val streamS: NamedColumnGetter[Stream[String]] = streamColumnGetter[String, String](identity)
  implicit val streamI: NamedColumnGetter[Stream[Int]] = streamColumnGetter[java.lang.Integer, Int](_.toInt)
  implicit val streamL: NamedColumnGetter[Stream[Long]] = streamColumnGetter[java.lang.Long, Long](_.toLong)
  implicit val streamD: NamedColumnGetter[Stream[Double]] = streamColumnGetter[java.lang.Double, Double](_.toDouble)
  implicit val streamF: NamedColumnGetter[Stream[Float]] = streamColumnGetter[java.lang.Float, Float](_.toFloat)

  implicit val mapSS: NamedColumnGetter[Map[String, String]] = mapColumnGetter[String, String, String](identity)
  implicit val mapSI: NamedColumnGetter[Map[String, Int]] = mapColumnGetter[String, java.lang.Integer, Int](_.toInt)
  implicit val mapSL: NamedColumnGetter[Map[String, Long]] = mapColumnGetter[String, java.lang.Long, Long](_.toLong)
  implicit val mapSD: NamedColumnGetter[Map[String, Double]] = mapColumnGetter[String, java.lang.Double, Double](_.toDouble)
  implicit val mapSF: NamedColumnGetter[Map[String, Float]] = mapColumnGetter[String, java.lang.Float, Float](_.toFloat)
}

trait LowPriorityNamedColumnGetter {

  implicit def optionColumnGetter[A](implicit A: NamedColumnGetter[A]) = new NamedColumnGetter[Option[A]] {
    def apply(row: Row, name: String): Option[A] =
      if (row.isNull(name)) None else Option(A(row, name))
  }

  def streamColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): NamedColumnGetter[Stream[A0]] =
    new NamedColumnGetter[Stream[A0]] {
      def apply(row: Row, name: String): Stream[A0] =
        row.getList(name, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toStream.map(f)
    }

  def seqColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): NamedColumnGetter[Seq[A0]] =
    new NamedColumnGetter[Seq[A0]] {
      def apply(row: Row, name: String): Seq[A0] =
        row.getList(name, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).map(_.asInstanceOf[A0])
    }

  def vectorColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): NamedColumnGetter[Vector[A0]] =
    new NamedColumnGetter[Vector[A0]] {
      def apply(row: Row, name: String): Vector[A0] =
        row.getList(name, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toVector.map(_.asInstanceOf[A0])
    }

  def listColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): NamedColumnGetter[List[A0]] =
    new NamedColumnGetter[List[A0]] {
      def apply(row: Row, name: String): List[A0] =
        row.getList(name, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toList.map(f)
    }

  def setColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): NamedColumnGetter[Set[A0]] =
    new NamedColumnGetter[Set[A0]] {
      def apply(row: Row, name: String): Set[A0] = {
        val x: Set[A] = row.getSet(name, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).toSet
        x.map(f)
      }
    }

  def mapColumnGetter[K, V, V0](f: V => V0)(
    implicit
    keyTag: ClassTag[K],
    valTag: ClassTag[V]
  ): NamedColumnGetter[Map[K, V0]] =
    new NamedColumnGetter[Map[K, V0]] {
      def apply(row: Row, name: String): Map[K, V0] =
        row.getMap(
          name,
          TypeToken.of[K](keyTag.runtimeClass.asInstanceOf[Class[K]]),
          TypeToken.of[V](keyTag.runtimeClass.asInstanceOf[Class[V]])
        ).toMap.mapValues(f)
    }
}
