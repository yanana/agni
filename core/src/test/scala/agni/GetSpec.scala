package agni

import cats.instances.try_._
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodecs
import com.datastax.oss.driver.api.core.cql.Row
import org.scalatest.{ FunSpec, Matchers }
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar

import scala.util.{ Failure, Success, Try }

class GetSpec extends FunSpec with MockitoSugar with Matchers {

  val version = ProtocolVersion.DEFAULT

  describe("getOneUnsafe") {
    it("should return specified type's value") {
      val row = mock[Row]
      when(row.getBytesUnsafe(0)).thenReturn(TypeCodecs.INT.encodePrimitive(10, version))
      val Success(r) = Get.getOneUnsafe[Int].apply[Try, Throwable](Iterable(row), version)
      assert(r === 10)
    }
    it("should return failure when the value obtained from Rows is empty") {
      val Failure(e) = Get.getOneUnsafe[Int].apply[Try, Throwable](Iterable.empty, version)
      e shouldBe a[Exception]
    }
  }

  describe("getOne") {
    it("should return specified type's value") {
      val row = mock[Row]
      when(row.getBytesUnsafe(0)).thenReturn(TypeCodecs.INT.encodePrimitive(10, version))
      val Success(Some(r)) = Get.getOne[Int].apply[Try, Throwable](Iterable(row), version)
      assert(r === 10)
    }
    it("should return None when the value obtained from Rows is empty") {
      val Success(x) = Get.getOne[Int].apply[Try, Throwable](Iterable.empty, version)
      assert(x === None)
    }
  }

  describe("getCBF") {
    it("should be build a Vector when specified type is Vector") {
      val row = mock[Row]
      when(row.getBytesUnsafe(0)).thenReturn(TypeCodecs.INT.encodePrimitive(1, version), TypeCodecs.INT.encodePrimitive(10, version), TypeCodecs.INT.encodePrimitive(100, version))
      val rows = Iterable(row, row, row)
      val Success(xs) = Get.getCBF[Int, Vector].apply[Try, Throwable](rows, version)
      assert(xs === Vector(1, 10, 100))
    }
    it("should be build a Set when specified type is Set") {
      val row = mock[Row]
      when(row.getBytesUnsafe(0)).thenReturn(TypeCodecs.INT.encodePrimitive(1, version), TypeCodecs.INT.encodePrimitive(10, version), TypeCodecs.INT.encodePrimitive(100, version))
      val rows = Iterable(row, row, row)
      val Success(xs) = Get.getCBF[Int, Set].apply[Try, Throwable](rows, version)
      assert(xs === Set(1, 10, 100))
    }
    it("should be build a Vector when specified type is List") {
      val row = mock[Row]
      when(row.getBytesUnsafe(0)).thenReturn(TypeCodecs.INT.encodePrimitive(1, version), TypeCodecs.INT.encodePrimitive(10, version), TypeCodecs.INT.encodePrimitive(100, version))
      val rows = Iterable(row, row, row)
      val Success(xs) = Get.getCBF[Int, List].apply[Try, Throwable](rows, version)
      assert(xs === List(1, 10, 100))
    }
  }

  describe("getUnit") {
    it("should return Unit") {
      val Success(x) = Get.getUnit.apply[Try, Throwable](Iterable.empty, version)
      assert(x.isInstanceOf[Unit])
    }
  }

  describe("getRowIterator") {
    it("should return `Iterator[Row]`") {
      val row = mock[Row]
      when(row.getBytesUnsafe(0)).thenReturn(TypeCodecs.INT.encodePrimitive(1, version), TypeCodecs.INT.encodePrimitive(10, version), TypeCodecs.INT.encodePrimitive(100, version))
      val rows = Iterable(row, row, row)
      val Success(xs) = Get.getRowIterator.apply[Try, Throwable](rows, version)
      xs shouldBe an[Iterator[Row]]
      assert(xs.toList === rows.toList)
    }
  }
}
