package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{ Date, UUID }

import cats.instances.either._
import cats.syntax.cartesian._
import cats.syntax.either._
import com.datastax.driver.core._

import scala.annotation.tailrec
import scala.collection.generic.IsTraversableOnce
import scala.collection.mutable

trait Serializer[A] { self =>

  def apply(value: A, version: ProtocolVersion): Result[ByteBuffer]

  def contramap[B](f: B => A): Serializer[B] = new Serializer[B] {
    override def apply(value: B, version: ProtocolVersion): Result[ByteBuffer] =
      self.apply(f(value), version)
  }
}

object Serializer {

  def apply[A](implicit A: Serializer[A]): Serializer[A] = A

  implicit def option[A](implicit A: Serializer[A]): Serializer[Option[A]] = new Serializer[Option[A]] {
    override def apply(value: Option[A], version: ProtocolVersion): Result[ByteBuffer] =
      value match {
        case None => Right(null)
        case Some(v) => A.apply(v, version)
      }
  }

  implicit val ascii: Serializer[String] = new Serializer[String] {
    override def apply(value: String, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.ascii().serialize(value, version))
  }

  implicit val cint: Serializer[Int] = new Serializer[Int] {
    override def apply(value: Int, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.cint().serialize(value, version))
  }

  implicit val bigint: Serializer[Long] = new Serializer[Long] {
    override def apply(value: Long, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.bigint().serialize(value, version))
  }

  implicit val cdouble: Serializer[Double] = new Serializer[Double] {
    override def apply(value: Double, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.cdouble().serialize(value, version))
  }

  implicit val cfloat: Serializer[Float] = new Serializer[Float] {
    override def apply(value: Float, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.cfloat().serialize(value, version))
  }

  implicit val bigDecimal: Serializer[BigDecimal] = new Serializer[BigDecimal] {
    override def apply(value: BigDecimal, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.decimal().serialize(value.bigDecimal, version))
  }

  implicit val tinyInt: Serializer[Byte] = new Serializer[Byte] {
    override def apply(value: Byte, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.tinyInt().serialize(value, version))
  }

  implicit val smallInt: Serializer[Short] = new Serializer[Short] {
    override def apply(value: Short, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.smallInt().serialize(value, version))
  }

  implicit val varint: Serializer[BigInt] = new Serializer[BigInt] {
    override def apply(value: BigInt, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.varint().serialize(value.bigInteger, version))
  }

  implicit val uuid: Serializer[UUID] = new Serializer[UUID] {
    override def apply(value: UUID, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.uuid().serialize(value, version))
  }

  implicit val blob: Serializer[ByteBuffer] = new Serializer[ByteBuffer] {
    override def apply(value: ByteBuffer, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.blob().serialize(value, version))
  }

  implicit val inet: Serializer[InetAddress] = new Serializer[InetAddress] {
    override def apply(value: InetAddress, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.inet().serialize(value, version))
  }

  implicit val date: Serializer[LocalDate] = new Serializer[LocalDate] {
    override def apply(value: LocalDate, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.date().serialize(value, version))
  }

  implicit val timestamp: Serializer[Date] = new Serializer[Date] {
    override def apply(value: Date, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.timestamp().serialize(value, version))
  }

  implicit val duration: Serializer[Duration] = new Serializer[Duration] {
    override def apply(value: Duration, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.duration().serialize(value, version))
  }

  // TODO: serializa timeUUID
  // TODO: serializa counter
  // TODO: serializa varchar

  implicit def map[M[K, V] <: Map[K, V], K, V](
    implicit
    K: Serializer[K],
    V: Serializer[V]
  ): Serializer[M[K, V]] = new Serializer[M[K, V]] {
    override def apply(value: M[K, V], version: ProtocolVersion): Result[ByteBuffer] = {
      @tailrec def go(m: List[(K, V)], acc: mutable.ArrayBuilder[ByteBuffer]): Result[Array[ByteBuffer]] = m match {
        case Nil => acc.result().asRight
        case (k, v) :: t =>
          val kv = (K.apply(k, version) |@| V.apply(v, version)).tupled
          kv match {
            case Left(e) => Left(e)
            case Right((k0, v0)) =>
              acc += k0
              acc += v0
              go(t, acc)
          }

      }

      val bbs = mutable.ArrayBuilder.make[ByteBuffer]()
      go(value.toList, bbs).flatMap(bbs => Either.catchNonFatal(CodecUtils.pack(bbs, value.size, version)))
    }
  }

  implicit def traversableOnce[A0, C[_]](
    implicit
    A: Serializer[A0],
    is: IsTraversableOnce[C[A0]] { type A = A0 }
  ): Serializer[C[A0]] =
    new Serializer[C[A0]] {
      override def apply(value: C[A0], version: ProtocolVersion): Result[ByteBuffer] = {
        if (value == null) Left(new NullPointerException) else {
          val items = mutable.ArrayBuilder.make[ByteBuffer]()
          val it = is.conversion(value).toIterator

          @tailrec def go(): Result[Array[ByteBuffer]] = {
            if (!it.hasNext) items.result().asRight
            else {
              A.apply(it.next(), version).map(b => items += b)
              go()
            }
          }

          go().flatMap(bbs => Either.catchNonFatal(CodecUtils.pack(bbs, bbs.length, version)))
        }
      }
    }
}
