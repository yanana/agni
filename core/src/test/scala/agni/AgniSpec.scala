package agni

import java.util

import cats.Id
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._
import org.scalacheck._
import scodec.bits.Arbitraries._
import scodec.bits.ByteVector

class AgniSpec extends Properties("Agni") {

  object Agni extends Agni[Id, Exception]

  property("convertStringToJava") = forAll { (a: String) =>
    Agni.convertToJava(a) == a
  }

  property("convertLongToJava") = forAll { (a: Long) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[java.lang.Long] && converted == a
  }

  property("convertIntToJava") = forAll { (a: Int) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[java.lang.Integer] && a == converted
  }

  property("convertFloatToJava") = forAll { (a: Float) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[java.lang.Float] && a == converted
  }

  property("convertDoubleToJava") = forAll { (a: Double) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[java.lang.Double] && a == converted
  }

  property("convertBigDecimalToJava") = forAll { (a: BigDecimal) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[java.math.BigDecimal] && a.bigDecimal == converted
  }

  property("convertBigIntToJava") = forAll { (a: BigInt) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[java.math.BigInteger] && a.bigInteger == converted
  }

  property("convertByteVectorToJava") = forAll { (a: ByteVector) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[Array[Byte]] && java.util.Arrays.equals(converted.asInstanceOf[Array[Byte]], a.toArray)
  }

  property("convertListToJava") = forAll { (a: List[Int]) =>
    val converted = Agni.convertToJava(a)
    val b = new util.ArrayList[Int]()
    a.foreach(b.add)
    converted.isInstanceOf[java.util.List[Object]] && b == converted
  }

  property("convertVectorToJava") = forAll { (a: Vector[Int]) =>
    val converted = Agni.convertToJava(a)
    val b = new util.ArrayList[Int]()
    a.foreach(b.add)
    converted.isInstanceOf[java.util.List[Object]] && b == converted
  }

  property("convertSeqToJava") = forAll { (a: Seq[String]) =>
    val converted = Agni.convertToJava(a)
    val b = new util.ArrayList[String]()
    a.foreach(b.add)
    converted.isInstanceOf[java.util.List[Object]] && b == converted
  }

  property("convertMapToJava") = forAll { (a: Map[String, Int]) =>
    val converted = Agni.convertToJava(a)
    val b = new util.HashMap[String, Integer]()
    a.foreach { case (k, v) => b.put(k, v) }
    converted.isInstanceOf[java.util.Map[Object, Object]] && b == converted
  }

  implicit def arbSome[T](implicit a: Arbitrary[T]): Arbitrary[Some[T]] = Arbitrary {
    for (e <- arbitrary[T]) yield Some(e)
  }

  property("convertSomeToJava") = forAll { (a: Some[BigInt]) =>
    val converted = Agni.convertToJava(a)
    converted.isInstanceOf[java.math.BigInteger] && a.get.bigInteger == converted
  }

  property("convertNoneToJava") = Prop {
    val converted = Agni.convertToJava(None)
    converted eq null
  }

  case class X(a: String, b: Int)

  def genX: Gen[X] = for {
    a <- arbitrary[String]
    b <- arbitrary[Int]
  } yield X(a, b)

  implicit def arbX: Arbitrary[X] = Arbitrary(genX)

  property("convetCaseClass") = forAll { (x: X) =>
    val converted = Agni.convertToJava(x)
    converted == x
  }

}
