package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.{ Instant, LocalDate, ZonedDateTime }
import java.util.UUID

import cats.instances.either._
import cats.syntax.apply._
import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodecs
import com.datastax.oss.driver.api.core.data.CqlDuration

import scala.annotation.tailrec
import scala.collection.generic.IsTraversableOnce
import scala.collection.mutable

trait Serializer[A] {
  self =>

  def apply(value: A, version: ProtocolVersion): Result[ByteBuffer]

  def contramap[B](f: B => A): Serializer[B] = new Serializer[B] {
    override def apply(value: B, version: ProtocolVersion): Result[ByteBuffer] =
      self.apply(f(value), version)
  }
}

object Serializer {

  def apply[A](implicit A: Serializer[A]): Serializer[A] = A

  implicit def serializeOption[A](implicit A: Serializer[A]): Serializer[Option[A]] = new Serializer[Option[A]] {
    override def apply(value: Option[A], version: ProtocolVersion): Result[ByteBuffer] =
      value match {
        case None => Right(null)
        case Some(v) => A.apply(v, version)
      }
  }

  implicit val serializeAscii: Serializer[String] = new Serializer[String] {
    override def apply(value: String, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.ASCII.encode(value, version))
  }

  implicit val serializeBoolean: Serializer[Boolean] = new Serializer[Boolean] {
    override def apply(value: Boolean, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.BOOLEAN.encodePrimitive(value.booleanValue(), version))
  }

  implicit val serializeInt: Serializer[Int] = new Serializer[Int] {
    override def apply(value: Int, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.INT.encodePrimitive(value.intValue(), version))
  }

  implicit val serializeLong: Serializer[Long] = new Serializer[Long] {
    override def apply(value: Long, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.BIGINT.encodePrimitive(value.longValue(), version))
  }

  implicit val serializeDouble: Serializer[Double] = new Serializer[Double] {
    override def apply(value: Double, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.DOUBLE.encodePrimitive(value, version))
  }

  implicit val serializeFloat: Serializer[Float] = new Serializer[Float] {
    override def apply(value: Float, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.FLOAT.encodePrimitive(value, version))
  }

  implicit val serializeBigDecimal: Serializer[BigDecimal] = new Serializer[BigDecimal] {
    override def apply(value: BigDecimal, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.DECIMAL.encode(value.bigDecimal, version))
  }

  implicit val serializeTinyInt: Serializer[Byte] = new Serializer[Byte] {
    override def apply(value: Byte, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.TINYINT.encode(value, version))
  }

  implicit val serializeSmallInt: Serializer[Short] = new Serializer[Short] {
    override def apply(value: Short, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.SMALLINT.encode(value, version))
  }

  implicit val serializeVarint: Serializer[BigInt] = new Serializer[BigInt] {
    override def apply(value: BigInt, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.VARINT.encode(value.bigInteger, version))
  }

  implicit val serializeUUID: Serializer[UUID] = new Serializer[UUID] {
    override def apply(value: UUID, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.UUID.encode(value, version))
  }

  implicit val serializeBlob: Serializer[ByteBuffer] = new Serializer[ByteBuffer] {
    override def apply(value: ByteBuffer, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.BLOB.encode(value, version))
  }

  implicit val serializeInet: Serializer[InetAddress] = new Serializer[InetAddress] {
    override def apply(value: InetAddress, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.INET.encode(value, version))
  }

  implicit val serializeDate: Serializer[LocalDate] = new Serializer[LocalDate] {
    override def apply(value: LocalDate, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.DATE.encode(value, version))
  }

  implicit val serializeTimestamp: Serializer[Instant] = new Serializer[Instant] {
    override def apply(value: Instant, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.TIMESTAMP.encode(value, version))
  }

  implicit val serializeDuration: Serializer[CqlDuration] = new Serializer[CqlDuration] {
    override def apply(value: CqlDuration, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.DURATION.encode(value, version))
  }

  implicit val serializeZonedDateTime: Serializer[ZonedDateTime] = new Serializer[ZonedDateTime] {
    override def apply(value: ZonedDateTime, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.ZONED_TIMESTAMP_UTC.encode(value, version))
  }

  // TODO: serializa timeUUID
  // TODO: serializa counter
  // TODO: serializa varchar

  implicit def serializeMapLike[M[K, V] <: Map[K, V], K, V](
    implicit
    K: Serializer[K],
    V: Serializer[V]
  ): Serializer[M[K, V]] = new Serializer[M[K, V]] {
    override def apply(value: M[K, V], version: ProtocolVersion): Result[ByteBuffer] = {
      @tailrec def go(m: List[(K, V)], acc: mutable.ArrayBuilder[ByteBuffer], toAllocate: Int): Result[(Array[ByteBuffer], Int)] = m match {
        case Nil => (acc.result(), toAllocate).asRight
        case (k, v) :: t =>
          val kv = (K(k, version), V(v, version)).mapN((_, _))
          kv match {
            case Left(e) => Left(e)
            case Right((k0, v0)) =>
              acc += k0
              acc += v0
              go(t, acc, toAllocate + 8 + k0.remaining() + v0.remaining())
          }

      }

      val bbs = mutable.ArrayBuilder.make[ByteBuffer]()
      go(value.toList, bbs, 4).flatMap {
        case (bbs, toAllocate) => Either.catchNonFatal {
          val result = ByteBuffer.allocate(toAllocate)
          result.putInt(value.size)
          bbs.foreach { buffer =>
            result.putInt(buffer.remaining())
            result.put(buffer)
          }
          result.flip()
          result
        }
      }
    }
  }

  implicit def serializeTraversableOnce[A0, C[_]](
    implicit
    A: Serializer[A0],
    is: IsTraversableOnce[C[A0]] { type A = A0 }
  ): Serializer[C[A0]] =
    new Serializer[C[A0]] {
      override def apply(value: C[A0], version: ProtocolVersion): Result[ByteBuffer] = {
        if (value == null) Left(new NullPointerException) else {
          val items = mutable.ArrayBuilder.make[ByteBuffer]()
          val it = is.conversion(value).toIterator

          @tailrec def go(toAllocate: Int): Result[(Array[ByteBuffer], Int)] = {
            if (!it.hasNext) (items.result(), toAllocate).asRight
            else {
              A.apply(it.next(), version) match {
                case Right(v) =>
                  items += v
                  go(toAllocate + 4 + v.remaining())
                case Left(e) =>
                  Left(e)
              }
            }
          }

          go(4).flatMap({
            case (bbs, toAllocate) => Either.catchNonFatal {
              val result = ByteBuffer.allocate(toAllocate)
              result.putInt(bbs.length)
              bbs.foreach { buffer =>
                result.putInt(buffer.remaining())
                result.put(buffer)
              }
              result.flip()
              result
            }
          })
        }
      }
    }
}
