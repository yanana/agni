package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.{ Date, UUID }

import com.datastax.driver.core.{ LocalDate, TupleValue, UDTValue, Row }
import scodec.bits.{ BitVector, ByteVector }
import shapeless._, syntax._, record._

class RowDecoderSpec extends org.scalatest.FunSuite {

  type A = Record.`'foo -> Int, 'bar -> Double, 'quux -> Vector[Int]`.T

  case class Quux(a: Int, b: Option[String], c: Map[String, String])

  test("RowDecoder[Int]") {
    assertCompiles("RowDecoder[Int]")
  }

  test("RowDecoder[String]") {
    assertCompiles("RowDecoder[String]")
  }

  test("RowDecoder[Long]") {
    assertCompiles("RowDecoder[Long]")
  }

  test("RowDecoder[Double]") {
    assertCompiles("RowDecoder[Double]")
  }

  test("RowDecoder[Float]") {
    assertCompiles("RowDecoder[Float]")
  }

  test("RowDecoder[BigDecimal]") {
    assertCompiles("RowDecoder[BigDecimal]")
  }

  test("RowDecoder[UUID]") {
    assertCompiles("RowDecoder[UUID]")
  }

  test("RowDecoder[Array[Byte]]") {
    assertCompiles("RowDecoder[Array[Byte]]")
  }

  test("RowDecoder[InetAddress]") {
    assertCompiles("RowDecoder[InetAddress]")
  }

  test("RowDecoder[LocalDate]") {
    assertCompiles("RowDecoder[LocalDate]")
  }

  test("RowDecoder[Instant]") {
    assertCompiles("RowDecoder[Instant]")
  }

  test("RowDecoder[BigInt]") {
    assertCompiles("RowDecoder[BigInt]")
  }

  test("RowDecoder[Seq[Int]]") {
    // assertCompiles("RowDecoder[Seq[Int]]")
  }

  test("RowDecoder[List[Int]]") {
    assertCompiles("RowDecoder[List[Int]]")
  }

  test("RowDecoder[Vector[String]]") {
    assertCompiles("RowDecoder[Vector[String]]")
  }

  test("RowDecoder[Set[String]]") {
    assertCompiles("RowDecoder[Set[String]]")
  }

  test("RowDecoder[Map[String, Date]]") {
    implicit val date = IndexedColumnGetter.mapColumnGetter[String, Date, Date](identity)
    assertCompiles("RowDecoder[Map[String, Date]]")
  }

  test("RowDecoder[Quux]") {
    assertCompiles("RowDecoder[Quux]")
  }

  test("RowDecoder[Record.`'foo -> Int, 'bar -> Double, 'quux -> Vector[Int]`]") {
    assertCompiles("RowDecoder[A]")
  }

  test("RowDecoder[UDTValue]") {
    assertCompiles("RowDecoder[UDTValue]")
  }

  test("RowDecoder[TupleValue]") {
    assertCompiles("RowDecoder[TupleValue]")
  }

  test("RowDecoder[ByteVector]") {
    assertCompiles("RowDecoder[ByteVector]")
  }

  test("RowDecoder[BitVector]") {
    assertCompiles("RowDecoder[BitVector]")
  }

  test("RowDecoderT2[(Int, Long)]") {
    assertCompiles("RowDecoder[(Int, Long)]")
  }

  test("RowDecoderT3[(Int, Long, Double)]") {
    assertCompiles("RowDecoder[(Int, Long, Double)]")
  }

  test("RowDecoderT4[(Int, Long, Double, String)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String)]")
  }

  test("RowDecoderT5[(Int, Long, Double, String, Option[Int])]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int])]")
  }

  test("RowDecoderT6[(Int, Long, Double, String, Option[Int], Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int)]")
  }

  test("RowDecoderT7[(Int, Long, Double, String, Option[Int], Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int)]")
  }

  test("RowDecoderT8[(Int, Long, Double, String, Option[Int], Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int)]")
  }

  test("RowDecoderT9[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int)]")
  }

  test("RowDecoderT10[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int)]")
  }

  test("RowDecoderT11[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long)]")
  }

  test("RowDecoderT12[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float)]")
  }

  test("RowDecoderT13[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double)]")
  }

  test("RowDecoderT14[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int)]")
  }

  test("RowDecoderT15[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int)]")
  }

  test("RowDecoderT16[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int)]")
  }

  test("RowDecoderT17[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]")
  }

  test("RowDecoderT18[(Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]")
  }

  test("RowDecoderT19[(Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]")
  }

  test("RowDecoderT20[(Int, Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]")
  }

  test("RowDecoderT21[(Int, Int, Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Int, Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]")
  }

  test("RowDecoderT22[(Int, Int, Int, Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]") {
    assertCompiles("RowDecoder[(Int, Int, Int, Int, Int, Int, Long, Double, String, Option[Int], Int, Int, Int, Int, Int, Long, Float, Double, Int, Int, Int, Int)]")
  }
}
