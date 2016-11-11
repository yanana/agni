import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{ Date, UUID, List => JList, Map => JMap, Set => JSet }

import com.datastax.driver.core.{ LocalDate, Token, TupleValue, UDTValue }
import shapeless._

import scala.collection.JavaConverters._

package object agni {
  import Function.const

  object scalaToJava extends Poly1 {
    implicit val caseString = at[String](identity)
    implicit val caseUUID = at[UUID](identity)
    implicit val caseInetAddress = at[InetAddress](identity)
    implicit val caseDate = at[Date](identity)
    implicit val caseLocalDate = at[LocalDate](identity)
    implicit val caseToken = at[Token](identity)
    implicit val caseUDTValue = at[UDTValue](identity)
    implicit val caseTupleValue = at[TupleValue](identity)
    implicit val caseByteBuffer = at[ByteBuffer](identity)
    implicit val caseBytes = at[Array[Byte]](ByteBuffer.wrap)
    implicit val caseLong = at[Long](Long.box)
    implicit val caseInt = at[Int](Int.box)
    implicit val caseFloat = at[Float](Float.box)
    implicit val caseDouble = at[Double](Double.box)
    implicit val caseBigDecimal = at[BigDecimal](_.bigDecimal)
    implicit val caseBigInt = at[BigInt](_.bigInteger)
    implicit def caseOptionSome[O[+A] <: Option[A], A, B](implicit sa: Case.Aux[A, B]) = at[O[A]](_.fold[B](null.asInstanceOf[B])(a => scalaToJava(a).asInstanceOf[B]))
    implicit val caseOptionNone = at[None.type](const(null))
    implicit def caseMap[M[K, +A] <: Map[K, A], A, B, K <: String](implicit sa: Case.Aux[A, B]) = at[M[K, A]](_.mapValues(scalaToJava).asJava: JMap[K, B])
    implicit def caseSet[L[A] <: Set[A], A, B](implicit sa: Case.Aux[A, B]) = at[L[A]](_.map(scalaToJava).asJava: JSet[B])
    implicit def caseSeq[L[+A] <: Seq[A], A, B](implicit sa: Case.Aux[A, B]) = at[L[A]](_.map(scalaToJava).asJava)
    implicit def caseVector[L[+A] <: Vector[A], A, B](implicit sa: Case.Aux[A, B]) = at[L[A]](_.map(scalaToJava).asJava)
    implicit def caseList[L[+A] <: List[A], A, B](implicit sa: Case.Aux[A, B]) = at[L[A]](_.map(scalaToJava).asJava: JList[B])
    implicit def caseStream[L[+A] <: Stream[A], A, B](implicit sa: Case.Aux[A, B]) = at[L[A]](_.map(scalaToJava).asJava: JList[B])
  }
}
