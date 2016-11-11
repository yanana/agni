package agni

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util
import java.util.{ Date, UUID }

import com.datastax.driver.core._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._
import org.scalacheck._

class scalaToJavaSpec extends Properties("scalaToJava") {

  property("caseString") = forAll { (a: String) =>
    scalaToJava(a) == a
  }

  property("caseUUID") = Prop {
    val a = UUID.randomUUID()
    scalaToJava(a) == a
  }

  property("caseInetAddress") = Prop {
    val a = InetAddress.getByName("127.0.0.1")
    scalaToJava(a) == a
  }

  property("caseDate") = Prop {
    val a = new Date(System.currentTimeMillis())
    scalaToJava(a) == a
  }

  property("caseLocalDate") = Prop {
    val a = LocalDate.fromMillisSinceEpoch(System.currentTimeMillis())
    scalaToJava(a) == a
  }

  property("caseLong") = forAll { (a: Long) =>
    val converted = scalaToJava(a)
    converted.isInstanceOf[java.lang.Long] && converted == a
  }

  property("caseInt") = forAll { (a: Int) =>
    val converted = scalaToJava(a)
    converted.isInstanceOf[java.lang.Integer] && a == converted
  }

  property("caseFloat") = forAll { (a: Float) =>
    val converted = scalaToJava(a)
    converted.isInstanceOf[java.lang.Float] && a == converted
  }

  property("caseDouble") = forAll { (a: Double) =>
    val converted = scalaToJava(a)
    converted.isInstanceOf[java.lang.Double] && a == converted
  }

  property("caseBigDecimal") = forAll { (a: BigDecimal) =>
    val converted = scalaToJava(a)
    converted.isInstanceOf[java.math.BigDecimal] && a.bigDecimal == converted
  }

  property("caseBigInt") = forAll { (a: BigInt) =>
    val converted = scalaToJava(a)
    converted.isInstanceOf[java.math.BigInteger] && a.bigInteger == converted
  }

  property("caseList") = forAll { (a: List[Int]) =>
    val converted = scalaToJava(a)
    val b = new util.ArrayList[Int]()
    a.foreach(b.add)
    converted.isInstanceOf[java.util.List[Object]] && b == converted
  }

  property("caseVector") = forAll { (a: Vector[Int]) =>
    val converted = scalaToJava(a)
    val b = new util.ArrayList[Int]()
    a.foreach(b.add)
    converted.isInstanceOf[java.util.List[Object]] && b == converted
  }

  property("caseSeq") = forAll { (a: Seq[String]) =>
    val converted = scalaToJava(a)
    val b = new util.ArrayList[String]()
    a.foreach(b.add)
    converted.isInstanceOf[java.util.List[Object]] && b == converted
  }

  property("caseMap") = forAll { (a: Map[String, Int]) =>
    val converted = scalaToJava(a)
    val b = new util.HashMap[String, Integer]()
    a.foreach { case (k, v) => b.put(k, v) }
    converted.isInstanceOf[java.util.Map[Object, Object]] && b == converted
  }

  implicit def arbSome[T](implicit a: Arbitrary[T]): Arbitrary[Some[T]] = Arbitrary {
    for (e <- arbitrary[T]) yield Some(e)
  }

  property("caseSome") = forAll { (a: Some[BigInt]) =>
    val converted = scalaToJava(a)
    converted.isInstanceOf[java.math.BigInteger] && a.get.bigInteger == converted
  }

  property("caseNone") = Prop {
    val converted = scalaToJava(None)
    converted eq null
  }
}
