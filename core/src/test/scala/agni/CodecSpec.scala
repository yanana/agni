package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets._
import java.time.temporal.ChronoUnit
import java.time.{ Instant, LocalDate }
import java.util.UUID

import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.data.CqlDuration
import org.scalacheck.{ Arbitrary, Gen, Prop, Shrink }
import org.scalatest.{ Assertion, FunSuite }
import org.scalatestplus.scalacheck.Checkers

class CodecSpec extends FunSuite with Checkers {

  implicit val arbString: Arbitrary[String] = Arbitrary(Gen.alphaStr)
  implicit val arbUUID: Arbitrary[UUID] = Arbitrary(Gen.uuid)
  implicit val arbByteBuffer: Arbitrary[ByteBuffer] =
    Arbitrary(Gen.alphaStr.map(a => ByteBuffer.wrap(a.getBytes(UTF_8))))
  implicit val byte: Arbitrary[Byte] = Arbitrary(Gen.choose[Int](0, 255).map(_.toByte))
  implicit val arbInetAddress: Arbitrary[InetAddress] =
    Arbitrary(Gen.resultOf[Byte, Byte, Byte, Byte, InetAddress] {
      case (a, b, c, d) => InetAddress.getByAddress(Array(a, b, c, d))
    })
  implicit val arbLocalDate: Arbitrary[LocalDate] =
    Arbitrary(Gen.const(LocalDate.ofEpochDay(Instant.now.getEpochSecond)))
  implicit val arbInstant: Arbitrary[Instant] =
    Arbitrary(Gen.const(Instant.now.truncatedTo(ChronoUnit.MILLIS)))
  implicit val arbDuration: Arbitrary[CqlDuration] =
    Arbitrary(Gen.resultOf[Int, Int, Long, CqlDuration] {
      case (a, b, c) => CqlDuration.newInstance(a, b, c)
    }(Arbitrary(Gen.posNum[Int]), Arbitrary(Gen.posNum[Int]), Arbitrary(Gen.posNum[Long])))

  def roundTrip[A: Deserializer: Serializer: Arbitrary: Shrink]: Assertion =
    check(Prop.forAll({ a: A =>
      val r = for {
        s <- Serializer[A].apply(a, ProtocolVersion.DEFAULT)
        d <- Deserializer[A].apply(s, ProtocolVersion.DEFAULT)
      } yield d === a
      r.fold(throw _, identity)
    }))

  test("Option[Int]")(roundTrip[Option[Int]])
  test("Int")(roundTrip[Int])
  test("Long")(roundTrip[Long])
  test("Float")(roundTrip[Float])
  test("Double")(roundTrip[Double])
  test("String")(roundTrip[String])
  test("BigInt")(roundTrip[BigInt])
  test("BigDecimal")(roundTrip[BigDecimal])
  test("Byte")(roundTrip[Byte])
  test("Short")(roundTrip[Short])
  test("UUID")(roundTrip[UUID])
  test("ByteBuffer")(roundTrip[ByteBuffer])
  test("InetAddress")(roundTrip[InetAddress])
  test("LocalDate")(roundTrip[LocalDate])
  test("Instant")(roundTrip[Instant])
  test("Duration")(roundTrip[CqlDuration])
  test("Map[String, Int]")(roundTrip[Map[String, Int]])
  test("Vector[Int]")(roundTrip[Vector[Int]])
  test("List[Int]")(roundTrip[List[Int]])
  test("Seq[Int]")(roundTrip[Seq[Int]])
  test("Set[Int]")(roundTrip[Set[Int]])
}
