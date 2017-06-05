package agni

import java.nio.ByteBuffer

import com.datastax.driver.core.ProtocolVersion
import org.scalatest.{ Assertion, FunSpec, Matchers }

class DeserializerSpec extends FunSpec with Matchers {

  def deserializeNull[A: Deserializer](empty: A): Assertion = {
    val r = Deserializer[A].apply(null.asInstanceOf[ByteBuffer], ProtocolVersion.NEWEST_SUPPORTED)
    val b = r.fold(throw _, _ === empty)
    assert(b)
  }

  describe("apply") {
    describe("Map[_, _]") {
      it("should return empty when passed buffer is null") {
        deserializeNull[Map[String, Int]](Map.empty)
      }
    }
    describe("Set[_]") {
      it("should return empty when passed buffer is null") {
        deserializeNull[Set[Int]](Set.empty)
      }
    }
    describe("Option[_]") {
      it("should return empty when passed buffer is null") {
        deserializeNull[Option[Int]](Option.empty[Int])
      }
    }
  }
}
