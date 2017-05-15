package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{ Date, UUID }

import com.datastax.driver.core._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import shapeless.record._

trait TypedSuite extends FunSuite with Checkers {

  final case class Named(
    oint: Option[Int],
    string: String,
    int: Int,
    long: Long,
    float: Float,
    double: Double,
    bigDecimal: BigDecimal,
    byte: Byte,
    short: Short,
    bigInt: BigInt,
    uuid: UUID,
    byteBuffer: ByteBuffer,
    inetAddress: InetAddress,
    localDate: LocalDate,
    date: Date,
    duration: Duration,
    listInt: List[Int],
    vectorString: Vector[String],
    setDouble: Set[Double],
    streamFloat: Stream[Float],
    mapIntString: Map[Int, String],
    tupleValue: TupleValue,
    udtValue: UDTValue
  )

  type T1 = Option[Int]
  type T2 = (Option[Int], String)
  type T3 = (Option[Int], String, Int)
  type T4 = (Option[Int], String, Int, Long)
  type T5 = (Option[Int], String, Int, Long, Float)
  type T6 = (Option[Int], String, Int, Long, Float, Double)
  type T7 = (Option[Int], String, Int, Long, Float, Double, BigDecimal)
  type T8 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte)
  type T9 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short)
  type T10 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt)
  type T11 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID)
  type T12 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer)
  type T13 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress)
  type T14 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate)
  type T15 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date)
  type T16 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration)
  type T17 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration, List[Int])
  type T18 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration, List[Int], Vector[String])
  type T19 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration, List[Int], Vector[String], Set[Double])
  type T20 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration, List[Int], Vector[String], Set[Double], Stream[Float])
  type T21 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration, List[Int], Vector[String], Set[Double], Stream[Float], Map[Int, String])
  type T22 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration, List[Int], Vector[String], Set[Double], Stream[Float], Map[Int, String], TupleValue)
  type T22_2 = (Option[Int], String, Int, Long, Float, Double, BigDecimal, Byte, Short, BigInt, UUID, ByteBuffer, InetAddress, LocalDate, Date, Duration, List[Int], Vector[String], Set[Double], Stream[Float], Map[Int, String], UDTValue)

  type IDV = Record.`'foo -> Int, 'bar -> Double, 'quux -> Vector[Int]`.T

}
