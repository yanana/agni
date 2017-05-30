package agni

import agni.cassandra.{ AResultSet, ARow, EmptyRow }
import cats.instances.try_._
import com.datastax.driver.core.{ ProtocolVersion, Row, TypeCodec }
import org.scalatest.{ FunSpec, Matchers }

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

class GetSpec extends FunSpec with Matchers {

  val version = ProtocolVersion.NEWEST_SUPPORTED

  describe("getOneUnsafe") {
    it("should return specified type's value") {
      val Success(r) = Get.getOneUnsafe[Int].apply[Try, Throwable](new AResultSet(Seq(ARow(TypeCodec.cint().serialize(10, version)))), version)
      assert(r === 10)
    }
    it("should return failure when the value obtained from Row is null") {
      val Failure(e) = Get.getOneUnsafe[Int].apply[Try, Throwable](new AResultSet(Seq(null.asInstanceOf[Row])), version)
      e shouldBe a[NullPointerException]
    }
  }

  describe("getOne") {
    it("should return specified type's value") {
      val Success(Some(r)) = Get.getOne[Int].apply[Try, Throwable](new AResultSet(Seq(ARow(TypeCodec.cint().serialize(10, version)))), version)
      assert(r === 10)
    }
    it("should return None when the value obtained from Row is null") {
      val Success(x) = Get.getOne[Int].apply[Try, Throwable](new AResultSet(Seq(null.asInstanceOf[Row])), version)
      assert(x === None)
    }
  }

  describe("getCBF") {
    it("should be build a Vector when specified type is Vector") {
      val rows = Seq(ARow(TypeCodec.cint().serialize(1, version)), ARow(TypeCodec.cint().serialize(10, version)), ARow(TypeCodec.cint().serialize(100, version)))
      val Success(xs) = Get.getCBF[Int, Vector].apply[Try, Throwable](new AResultSet(rows), version)
      assert(xs === Vector(1, 10, 100))
    }
    it("should be build a Set when specified type is Set") {
      val rows = Seq(ARow(TypeCodec.cint().serialize(1, version)), ARow(TypeCodec.cint().serialize(10, version)), ARow(TypeCodec.cint().serialize(100, version)))
      val Success(xs) = Get.getCBF[Int, Set].apply[Try, Throwable](new AResultSet(rows), version)
      assert(xs === Set(1, 10, 100))
    }
    it("should be build a Vector when specified type is List") {
      val rows = Seq(ARow(TypeCodec.cint().serialize(1, version)), ARow(TypeCodec.cint().serialize(10, version)), ARow(TypeCodec.cint().serialize(100, version)))
      val Success(xs) = Get.getCBF[Int, List].apply[Try, Throwable](new AResultSet(rows), version)
      assert(xs === List(1, 10, 100))
    }
  }

  describe("getUnit") {
    it("should return Unit") {
      val Success(x) = Get.getUnit.apply[Try, Throwable](new AResultSet(Seq(new EmptyRow)), version)
      assert(x.isInstanceOf[Unit])
    }
  }

  describe("getRowIterator") {
    it("should return `Iterator[Row]`") {
      val rows = Seq(ARow(TypeCodec.cint().serialize(1, version)), ARow(TypeCodec.cint().serialize(10, version)), ARow(TypeCodec.cint().serialize(100, version)))
      val Success(xs) = Get.getRowIterator.apply[Try, Throwable](new AResultSet(rows), version)
      xs shouldBe an[Iterator[Row]]
      assert(xs.toList === rows.toList)
    }
  }

  describe("getRowJavaStream") {
    it("should return `java.util.stream.Stream[Row]`") {
      val rows = Seq(ARow(TypeCodec.cint().serialize(1, version)), ARow(TypeCodec.cint().serialize(10, version)), ARow(TypeCodec.cint().serialize(100, version)))
      val Success(xs) = Get.getRowJavaStream.apply[Try, Throwable](new AResultSet(rows), version)
      xs shouldBe a[java.util.stream.Stream[Row]]
      assert(xs.iterator().asScala.toList === rows.toList)
    }
  }
}
