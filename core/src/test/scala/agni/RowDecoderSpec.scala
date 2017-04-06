package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.{ Date, UUID }

import agni.cassandra.EmptyRow
import com.datastax.driver.core.{ LocalDate, Token, TupleValue, UDTValue }
import org.scalatest.{ Assertion, FunSuite }
import org.scalatest.prop.Checkers
import shapeless.record._

class RowDecoderSpec extends FunSuite with Checkers {

  final case class Named(a: Option[Int], b: ByteBuffer, c: Int, d: String, e: Long, f: Double, g: Float, h: BigDecimal, i: UUID, j: Array[Byte], k: InetAddress, l: LocalDate, m: Instant, n: BigInt, o: Date, p: Token, q: List[Int], r: Vector[String], s: Set[Double], t: Map[String, Date], u: UDTValue, v: TupleValue, w: Stream[Float])

  type IDV = Record.`'foo -> Int, 'bar -> Double, 'quux -> Vector[Int]`.T

  implicit val iDateGetter: IndexedColumnGetter[Map[String, Date]] =
    IndexedColumnGetter.mapColumnGetter[String, Date, Date](identity)
  implicit val nDateGetter: NamedColumnGetter[Map[String, Date]] =
    NamedColumnGetter.mapColumnGetter[String, Date, Date](identity)

  def checkType[A: RowDecoder]: Assertion = {
    val decoder = RowDecoder.apply[A]
    decoder.apply(new EmptyRow) match {
      case Left(e) =>
        e.printStackTrace(); fail(e)
      case Right(x) => assert(x.isInstanceOf[A])
    }

  }

  // NamedColumnGetter
  test("RowDecoder[Named]")(checkType[Named])
  test("RowDecoder[Record.`'foo -> Int, 'bar -> Double, 'quux -> Vector[Int]`]")(checkType[IDV])

  // IndexedColumnGetter
  test("RowDecoder[Option[Int]]")(checkType[Option[Int]])
  test("RowDecoder[Stream[Int]]")(checkType[Stream[Int]])
  test("RowDecoder[(ByteBuffer, Int)]")(checkType[(ByteBuffer, Int)])
  test("RowDecoder[(ByteBuffer, Int, String)]")(checkType[(ByteBuffer, Int, String)])
  test("RowDecoder[(ByteBuffer, Int, String, Long)]")(checkType[(ByteBuffer, Int, String, Long)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double)]")(checkType[(ByteBuffer, Int, String, Long, Double)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte])]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte])])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token)]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int])]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int])])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String])]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String])])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String])]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String])])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String], Map[String, Date])]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String], Map[String, Date])])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String], Map[String, Date], UDTValue])]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String], Map[String, Date], UDTValue)])
  test("RowDecoder[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String], Map[String, Date], UDTValue, TupleValue])]")(checkType[(ByteBuffer, Int, String, Long, Double, Float, BigDecimal, UUID, Array[Byte], InetAddress, LocalDate, Instant, BigInt, Date, Token, List[Int], Vector[String], Set[String], Map[String, Date], UDTValue, TupleValue)])
}
