package agni

import java.nio.ByteBuffer

import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import org.scalatest.{ Assertion, FunSpec, Matchers }

class DeserializerSpec extends FunSpec with Matchers {

  val protoVer = ProtocolVersion.DEFAULT

  def deserializeNull[A: Deserializer](empty: A): Assertion = {
    val r = Deserializer[A].apply(null.asInstanceOf[ByteBuffer], protoVer)
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

  describe("map") {
    it("should return the value which applied the function") {
      val int = 10
      val des = Deserializer[Int].map(a => a.toString)
      val x = for {
        v <- Serializer[Int].apply(int, protoVer)
        r <- des.apply(v, protoVer)
      } yield r

      x match {
        case Left(e) => fail(e)
        case Right(v) => assert(int.toString === v)
      }
    }
  }

  describe("flatMap") {
    it("should return the value which applied the function") {
      val int = 10
      val des = Deserializer[Int].flatMap(a => Deserializer.const(a.toString))
      val x = for {
        v <- Serializer[Int].apply(int, protoVer)
        r <- des.apply(v, protoVer)
      } yield r

      x match {
        case Left(e) => fail(e)
        case Right(v) => assert(int.toString === v)
      }
    }
  }
}
