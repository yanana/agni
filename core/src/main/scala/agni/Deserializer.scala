package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.{ Instant, LocalDate }
import java.util.UUID

import cats.instances.either._
import cats.syntax.apply._
import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodecs
import com.datastax.oss.driver.api.core.data.CqlDuration

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom

trait Deserializer[A] {
  self =>

  def apply(raw: ByteBuffer, version: ProtocolVersion): Result[A]

  def map[B](f: A => B): Deserializer[B] = new Deserializer[B] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[B] =
      self.apply(raw, version).map(f)
  }

  def flatMap[B](f: A => Deserializer[B]): Deserializer[B] = new Deserializer[B] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[B] =
      self.apply(raw, version).flatMap(f(_)(raw, version))
  }
}

object Deserializer {

  def apply[A](implicit A: Deserializer[A]): Deserializer[A] = A

  def const[A](b: A): Deserializer[A] = new Deserializer[A] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[A] = Right(b)
  }

  def failed[A](ex: Throwable): Deserializer[A] = new Deserializer[A] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[A] = Left(ex)
  }

  implicit def deserializeOption[A](implicit A: Deserializer[A]): Deserializer[Option[A]] = new Deserializer[Option[A]] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Option[A]] =
      if (raw == null) Right(None) else A.apply(raw, version).map(Some(_))
  }

  implicit val deserializeAscii: Deserializer[String] = new Deserializer[String] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[String] =
      Either.catchNonFatal(TypeCodecs.ASCII.decode(raw, version))
  }

  implicit val deserializeBoolean: Deserializer[Boolean] = new Deserializer[Boolean] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Boolean] =
      Either.catchNonFatal(TypeCodecs.BOOLEAN.decodePrimitive(raw, version))
  }

  implicit val deserializeInt: Deserializer[Int] = new Deserializer[Int] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Int] =
      Either.catchNonFatal(TypeCodecs.INT.decodePrimitive(raw, version))
  }

  implicit val deserializeBigint: Deserializer[Long] = new Deserializer[Long] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Long] =
      Either.catchNonFatal(TypeCodecs.BIGINT.decodePrimitive(raw, version))
  }

  implicit val deserializeDouble: Deserializer[Double] = new Deserializer[Double] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Double] =
      Either.catchNonFatal(TypeCodecs.DOUBLE.decode(raw, version))
  }

  implicit val deserializecfloat: Deserializer[Float] = new Deserializer[Float] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Float] =
      Either.catchNonFatal(TypeCodecs.FLOAT.decodePrimitive(raw, version))
  }

  implicit val deserializebigDecimal: Deserializer[BigDecimal] = new Deserializer[BigDecimal] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[BigDecimal] =
      Either.catchNonFatal(TypeCodecs.DECIMAL.decode(raw, version))
  }

  implicit val deserializeTinyInt: Deserializer[Byte] = new Deserializer[Byte] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Byte] =
      Either.catchNonFatal(TypeCodecs.TINYINT.decodePrimitive(raw, version))
  }

  implicit val deserializeSmallInt: Deserializer[Short] = new Deserializer[Short] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Short] =
      Either.catchNonFatal(TypeCodecs.SMALLINT.decodePrimitive(raw, version))
  }

  implicit val deserializeVarint: Deserializer[BigInt] = new Deserializer[BigInt] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[BigInt] =
      Either.catchNonFatal(TypeCodecs.VARINT.decode(raw, version))
  }

  implicit val deserializeUUID: Deserializer[UUID] = new Deserializer[UUID] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[UUID] =
      Either.catchNonFatal(TypeCodecs.UUID.decode(raw, version))
  }

  implicit val deserializeBlob: Deserializer[ByteBuffer] = new Deserializer[ByteBuffer] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodecs.BLOB.decode(raw, version))
  }

  implicit val deserializeInet: Deserializer[InetAddress] = new Deserializer[InetAddress] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[InetAddress] =
      Either.catchNonFatal(TypeCodecs.INET.decode(raw, version))
  }

  implicit val deserializeDate: Deserializer[LocalDate] = new Deserializer[LocalDate] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[LocalDate] =
      Either.catchNonFatal(TypeCodecs.DATE.decode(raw, version))
  }

  implicit val deserializeTimestamp: Deserializer[Instant] = new Deserializer[Instant] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Instant] =
      Either.catchNonFatal(TypeCodecs.TIMESTAMP.decode(raw, version))
  }

  implicit val deserializeDuration: Deserializer[CqlDuration] = new Deserializer[CqlDuration] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[CqlDuration] =
      Either.catchNonFatal(TypeCodecs.DURATION.decode(raw, version))
  }

  // TODO: deserializer timeUUID
  // TODO: deserializer counter
  // TODO: deserializer varchar

  private def read(input: ByteBuffer): ByteBuffer = {
    val size = input.getInt()
    val encoded = input.slice()
    encoded.limit(size)
    input.position(input.position() + size)
    encoded
  }

  implicit def deserializeMap[M[K, +V] <: Map[K, V], K, V](
    implicit
    K: Deserializer[K],
    V: Deserializer[V],
    cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]
  ): Deserializer[M[K, V]] = new Deserializer[M[K, V]] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[M[K, V]] = {
      val builder = cbf.apply
      if (raw == null || !raw.hasRemaining)
        builder.result().asRight[Throwable]
      else {
        val input = raw.duplicate()

        @tailrec def go(size: Int): Result[M[K, V]] =
          if (size == 0) builder.result().asRight
          else {
            val encodedKey = read(input)
            val encodedValue = read(input)

            (K(encodedKey, version), V(encodedValue, version)).mapN {
              case (k, v) =>
                builder += k -> v
            } match {
              case Right(_) => go(size - 1)
              case Left(e) => Left(e)
            }
          }

        val size = input.getInt()
        builder.sizeHint(size)

        Either.catchNonFatal(size).flatMap(go)
      }
    }
  }

  implicit def deserializeCollection[A, C[_]](implicit A: Deserializer[A], cbf: CanBuildFrom[Nothing, A, C[A]]): Deserializer[C[A]] =
    new Deserializer[C[A]] {
      override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[C[A]] = {
        val builder = cbf.apply()
        if (raw == null || !raw.hasRemaining)
          builder.result().asRight
        else {
          val input = raw.duplicate()

          @tailrec def go(size: Int): Result[C[A]] =
            if (size == 0) builder.result().asRight
            else {
              A(read(input), version) match {
                case Right(v) =>
                  builder += v
                  go(size - 1)
                case Left(e) =>
                  Left(e)
              }
            }

          val size = input.getInt()
          builder.sizeHint(size)

          Either.catchNonFatal(size).flatMap(go)
        }
      }
    }
}
