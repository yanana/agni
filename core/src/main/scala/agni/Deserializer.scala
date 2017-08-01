package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{ Date, UUID }

import cats.instances.either._
import cats.syntax.cartesian._
import cats.syntax.either._
import com.datastax.driver.core._

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

trait Deserializer[A] { self =>

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

  implicit def option[A](implicit A: Deserializer[A]): Deserializer[Option[A]] = new Deserializer[Option[A]] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Option[A]] =
      if (raw == null) Right(None) else A.apply(raw, version).map(Some(_))
  }

  implicit val ascii: Deserializer[String] = new Deserializer[String] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[String] =
      Either.catchNonFatal(TypeCodec.ascii().deserialize(raw, version))
  }

  implicit val cint: Deserializer[Int] = new Deserializer[Int] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Int] =
      Either.catchNonFatal(TypeCodec.cint().deserialize(raw, version))
  }

  implicit val bigint: Deserializer[Long] = new Deserializer[Long] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Long] =
      Either.catchNonFatal(TypeCodec.bigint().deserialize(raw, version))
  }

  implicit val cdouble: Deserializer[Double] = new Deserializer[Double] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Double] =
      Either.catchNonFatal(TypeCodec.cdouble().deserialize(raw, version))
  }

  implicit val cfloat: Deserializer[Float] = new Deserializer[Float] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Float] =
      Either.catchNonFatal(TypeCodec.cfloat().deserialize(raw, version))
  }

  implicit val bigDecimal: Deserializer[BigDecimal] = new Deserializer[BigDecimal] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[BigDecimal] =
      Either.catchNonFatal(TypeCodec.decimal().deserialize(raw, version))
  }

  implicit val tinyInt: Deserializer[Byte] = new Deserializer[Byte] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Byte] =
      Either.catchNonFatal(TypeCodec.tinyInt().deserialize(raw, version))
  }

  implicit val smallInt: Deserializer[Short] = new Deserializer[Short] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Short] =
      Either.catchNonFatal(TypeCodec.smallInt().deserialize(raw, version))
  }

  implicit val varint: Deserializer[BigInt] = new Deserializer[BigInt] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[BigInt] =
      Either.catchNonFatal(TypeCodec.varint().deserialize(raw, version))
  }

  implicit val uuid: Deserializer[UUID] = new Deserializer[UUID] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[UUID] =
      Either.catchNonFatal(TypeCodec.uuid().deserialize(raw, version))
  }

  implicit val blob: Deserializer[ByteBuffer] = new Deserializer[ByteBuffer] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[ByteBuffer] =
      Either.catchNonFatal(TypeCodec.blob().deserialize(raw, version))
  }

  implicit val inet: Deserializer[InetAddress] = new Deserializer[InetAddress] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[InetAddress] =
      Either.catchNonFatal(TypeCodec.inet().deserialize(raw, version))
  }

  implicit val date: Deserializer[LocalDate] = new Deserializer[LocalDate] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[LocalDate] =
      Either.catchNonFatal(TypeCodec.date().deserialize(raw, version))
  }

  implicit val timestamp: Deserializer[Date] = new Deserializer[Date] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Date] =
      Either.catchNonFatal(TypeCodec.timestamp().deserialize(raw, version))
  }

  implicit val duration: Deserializer[Duration] = new Deserializer[Duration] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[Duration] =
      Either.catchNonFatal(TypeCodec.duration().deserialize(raw, version))
  }

  // TODO: deserializer timeUUID
  // TODO: deserializer counter
  // TODO: deserializer varchar

  implicit def map[M[K, +V] <: Map[K, V], K, V](
    implicit
    K: Deserializer[K],
    V: Deserializer[V],
    cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): Deserializer[M[K, V]] = new Deserializer[M[K, V]] {
    override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[M[K, V]] = {
      val builder = cbf.apply
      if (raw == null || !raw.hasRemaining)
        builder.result().asRight[Throwable]
      else {
        @tailrec def go(size: Int, input: ByteBuffer, acc: mutable.Builder[(K, V), M[K, V]]): Result[M[K, V]] =
          size match {
            case 0 => builder.result().asRight
            case x =>
              val kbb = CodecUtils.readValue(input, version)
              val vbb = CodecUtils.readValue(input, version)
              val r = (K.apply(kbb, version) |@| V.apply(vbb, version)).map {
                case (k, v) =>
                  acc += k -> v
              }
              r match {
                case Left(e) => Left(e)
                case Right(_) => go(x - 1, input, acc)
              }
          }

        val input = raw.duplicate()
        for {
          s <- Either.catchNonFatal(CodecUtils.readSize(input, version))
          r <- go(s, input, builder)
        } yield r
        builder.result().asRight
      }
    }
  }

  implicit def collection[A, C[_]](implicit A: Deserializer[A], cbf: CanBuildFrom[Nothing, A, C[A]]): Deserializer[C[A]] =
    new Deserializer[C[A]] {
      override def apply(raw: ByteBuffer, version: ProtocolVersion): Result[C[A]] = {
        val builder = cbf.apply()
        if (raw == null || !raw.hasRemaining)
          builder.result().asRight
        else {
          @tailrec def go(size: Int, input: ByteBuffer, acc: mutable.Builder[A, C[A]]): Result[C[A]] =
            size match {
              case 0 =>
                builder.result().asRight
              case x =>
                val toAppend = CodecUtils.readValue(input, version)
                A.apply(toAppend, version) match {
                  case Left(e) =>
                    Left(e)
                  case Right(v) =>
                    acc += v
                    go(x - 1, input, acc)
                }
            }

          val input = raw.duplicate()
          for {
            s <- Either.catchNonFatal(CodecUtils.readSize(input, version))
            r <- go(s, input, builder)
          } yield r
        }
      }
    }
}
