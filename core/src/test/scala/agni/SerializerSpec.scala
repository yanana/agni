package agni

import cats.syntax.either._
import com.datastax.oss.driver.api.core.ProtocolVersion
import org.scalatest.{ FunSpec, Matchers }

class SerializerSpec extends FunSpec with Matchers {

  describe("contramap") {

    it("should return the value which applied the function") {
      val int = 123

      val x = for {
        v <- Serializer[String].contramap[Int](a => a.toString).apply(int, ProtocolVersion.DEFAULT)
        r <- Deserializer[String].apply(v, ProtocolVersion.DEFAULT)
      } yield r

      x match {
        case Left(e) => fail(e)
        case Right(v) => assert(int.toString === v)
      }
    }
  }
}
