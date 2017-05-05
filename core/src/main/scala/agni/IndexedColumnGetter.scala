package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.{ Date, UUID }

import cats.syntax.either._
import com.datastax.driver.core._
import com.google.common.reflect.TypeToken

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

trait IndexedColumnGetter[+T] {
  def apply(row: Row, i: Int): Result[T]
}

object IndexedColumnGetter extends LowPriorityIndexedColumnGetter {
  def apply[T](implicit T: IndexedColumnGetter[T]): IndexedColumnGetter[T] = T

  implicit val stringColumnGetter = new IndexedColumnGetter[String] {
    def apply(row: Row, i: Int): Result[String] = Either.catchNonFatal(row.getString(i))
  }

  implicit val intColumnGetter = new IndexedColumnGetter[Int] {
    def apply(row: Row, i: Int): Result[Int] = Either.catchNonFatal(row.getInt(i))
  }

  implicit val longColumnGetter: IndexedColumnGetter[Long] =
    new IndexedColumnGetter[Long] {
      def apply(row: Row, i: Int): Result[Long] = Either.catchNonFatal(row.getLong(i))
    }

  implicit val doubleColumnGetter: IndexedColumnGetter[Double] =
    new IndexedColumnGetter[Double] {
      def apply(row: Row, i: Int): Result[Double] = Either.catchNonFatal(row.getDouble(i))
    }

  implicit val floatColumnGetter: IndexedColumnGetter[Float] =
    new IndexedColumnGetter[Float] {
      def apply(row: Row, i: Int): Result[Float] = Either.catchNonFatal(row.getFloat(i))
    }

  implicit val bigDecimalColumnGetter: IndexedColumnGetter[BigDecimal] =
    new IndexedColumnGetter[BigDecimal] {
      def apply(row: Row, i: Int): Result[BigDecimal] = Either.catchNonFatal(row.getDecimal(i))
    }

  implicit val uuidColumnGetter: IndexedColumnGetter[UUID] =
    new IndexedColumnGetter[UUID] {
      def apply(row: Row, i: Int): Result[UUID] = Either.catchNonFatal(row.getUUID(i))
    }

  implicit val bytesColumnGetter: IndexedColumnGetter[Array[Byte]] =
    new IndexedColumnGetter[Array[Byte]] {
      def apply(row: Row, i: Int): Result[Array[Byte]] = Either.catchNonFatal(row.getBytes(i).array())
    }

  implicit val byteBufferColumnGetter: IndexedColumnGetter[ByteBuffer] =
    new IndexedColumnGetter[ByteBuffer] {
      def apply(row: Row, i: Int): Result[ByteBuffer] = Either.catchNonFatal(row.getBytes(i))
    }

  implicit val inetColumnGetter: IndexedColumnGetter[InetAddress] =
    new IndexedColumnGetter[InetAddress] {
      def apply(row: Row, i: Int): Result[InetAddress] = Either.catchNonFatal(row.getInet(i))
    }

  implicit val localDateColumnGetter: IndexedColumnGetter[LocalDate] =
    new IndexedColumnGetter[LocalDate] {
      def apply(row: Row, i: Int): Result[LocalDate] = Either.catchNonFatal(row.getDate(i))
    }

  implicit val timestampColumnGetter: IndexedColumnGetter[Date] =
    new IndexedColumnGetter[Date] {
      def apply(row: Row, i: Int): Result[Date] = Either.catchNonFatal(row.getTimestamp(i))
    }

  implicit val tokenColumnGetter: IndexedColumnGetter[Token] =
    new IndexedColumnGetter[Token] {
      def apply(row: Row, i: Int): Result[Token] = Either.catchNonFatal(row.getToken(i))
    }

  implicit val instantColumnGetter: IndexedColumnGetter[Instant] =
    new IndexedColumnGetter[Instant] {
      def apply(row: Row, i: Int): Result[Instant] = Either.catchNonFatal(row.getTimestamp(i).toInstant)
    }

  implicit val varintColumnGetter: IndexedColumnGetter[BigInt] =
    new IndexedColumnGetter[BigInt] {
      def apply(row: Row, i: Int): Result[BigInt] = Either.catchNonFatal(row.getVarint(i))
    }

  implicit val udtValueColumnGetter: IndexedColumnGetter[UDTValue] =
    new IndexedColumnGetter[UDTValue] {
      def apply(row: Row, i: Int): Result[UDTValue] = Either.catchNonFatal(row.getUDTValue(i))
    }

  implicit val tupleValueColumnGetter: IndexedColumnGetter[TupleValue] =
    new IndexedColumnGetter[TupleValue] {
      def apply(row: Row, i: Int): Result[TupleValue] = Either.catchNonFatal(row.getTupleValue(i))
    }

  def mapColumnGetter[K, V, V0](f: V => V0)(
    implicit
    keyTag: ClassTag[K],
    valTag: ClassTag[V]
  ): IndexedColumnGetter[Map[K, V0]] =
    new IndexedColumnGetter[Map[K, V0]] {
      def apply(row: Row, i: Int): Result[Map[K, V0]] = Either.catchNonFatal {
        row.getMap(
          i,
          TypeToken.of[K](keyTag.runtimeClass.asInstanceOf[Class[K]]),
          TypeToken.of[V](valTag.runtimeClass.asInstanceOf[Class[V]])
        ).asScala.toMap.mapValues(f)
      }
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
    def apply(row: Row, i: Int): Result[Option[A]] =
      if (row.isNull(i)) Right(None) else A(row, i).map(Option.apply)
  }

  def streamColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Stream[A0]] =
    new IndexedColumnGetter[Stream[A0]] {
      def apply(row: Row, i: Int): Result[Stream[A0]] = Either.catchNonFatal {
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).asScala.toStream.map(f)
      }
    }

  def seqColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Seq[A0]] =
    new IndexedColumnGetter[Seq[A0]] {
      def apply(row: Row, i: Int): Result[Seq[A0]] = Either.catchNonFatal {
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).asScala.map(_.asInstanceOf[A0])
      }
    }

  def vectorColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Vector[A0]] =
    new IndexedColumnGetter[Vector[A0]] {
      def apply(row: Row, i: Int): Result[Vector[A0]] = Either.catchNonFatal {
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).asScala.toVector.map(_.asInstanceOf[A0])
      }
    }

  def listColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[List[A0]] =
    new IndexedColumnGetter[List[A0]] {
      def apply(row: Row, i: Int): Result[List[A0]] = Either.catchNonFatal {
        row.getList(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).asScala.toList.map(f)
      }
    }

  def setColumnGetter[A, A0](f: A => A0)(implicit tag: ClassTag[A]): IndexedColumnGetter[Set[A0]] =
    new IndexedColumnGetter[Set[A0]] {
      def apply(row: Row, i: Int): Result[Set[A0]] = Either.catchNonFatal {
        val x: Set[A] = row.getSet(i, TypeToken.of[A](tag.runtimeClass.asInstanceOf[Class[A]])).asScala.toSet
        x.map(f)
      }
    }
}
