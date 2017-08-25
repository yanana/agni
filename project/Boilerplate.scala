import sbt._

/**
 * Generate a range of boilerplate classes that would be tedious to write and maintain by hand.
 *
 * Copied, with some modifications, from
 * [[https://github.com/milessabin/shapeless/blob/master/project/Boilerplate.scala Shapeless]].
 *
 * @author Miles Sabin
 * @author Kevin Wright
 */
object Boilerplate {

  import scala.StringContext._

  implicit class BlockHelper(val sc: StringContext) extends AnyVal {
    def block(args: Any*): String = {
      val interpolated = sc.standardInterpolator(treatEscapes, args)
      val rawLines = interpolated split '\n'
      val trimmedLines = rawLines map { _ dropWhile (_.isWhitespace) }
      trimmedLines mkString "\n"
    }
  }

  val templates: Seq[Template] = List(
    GenTupleRowDecoder,
    GenTupleBinder
  )

  /** Returns a seq of the generated files.  As a side-effect, it actually generates them... */
  def gen(dir : File) = for(t <- templates) yield {
    val tgtFile = t.file(dir)
    IO.write(tgtFile, t.body)
    tgtFile
  }

  val header = ""
  val maxArity = 22

  class TemplateVals(val arity: Int) {
    val synTypes     = (0 until arity) map (n => (n+'A').toChar)
    val synVals      = (0 until arity) map (n => (n+'a').toChar)

    val `A..N`       = synTypes.mkString(", ")
    val `(A..N)`     = if (arity == 1) "Tuple1[A]" else synTypes.mkString("(", ", ", ")")
    def `a:F[A]..n:F[N]`(f: String) = (synVals zip synTypes) map { case (v,t) => s"$v: $f[$t]" } mkString ", "
  }

  trait Template {
    def file(root: File): File
    def content(tv: TemplateVals): String
    def range = 1 to maxArity
    def body: String = {
      val headerLines = header split '\n'
      val rawContents = range map { n => content(new TemplateVals(n)) split '\n' filterNot (_.isEmpty) }
      val preBody = rawContents.head takeWhile (_ startsWith "|") map (_.tail)
      val instances = rawContents flatMap {_ filter (_ startsWith "-") map (_.tail) }
      val postBody = rawContents.head dropWhile (_ startsWith "|") dropWhile (_ startsWith "-") map (_.tail)
      (headerLines ++ preBody ++ instances ++ postBody) mkString "\n"
    }
  }


  /*
    Blocks in the templates below use a custom interpolator, combined with post-processing to produce the body

      - The contents of the `header` val is output first

      - Then the first block of lines beginning with '|'

      - Then the block of lines beginning with '-' is replicated once for each arity,
        with the `templateVals` already pre-populated with relevant relevant vals for that arity

      - Then the last block of lines prefixed with '|'

    The block otherwise behaves as a standard interpolated string with regards to variable substitution.
  */

  object GenTupleRowDecoder extends Template {
    def file(root: File) = root / "agni" / "TupleRowDecoder.scala"
    def content(tv: TemplateVals) = {
      import tv._
      val expr = (synVals zipWithIndex) map { case (v,i) => s"$v(row, $i, ver)" }
      val cartesian = expr mkString " |@| "
      val tupled = if (expr.size == 1) s"${cartesian}.map(Tuple1(_))" else s"(${cartesian}).tupled"
      block"""
        |package agni
        |
        |import cats.instances.either._
        |import cats.syntax.either._
        |import cats.syntax.cartesian._
        |import com.datastax.driver.core.{ ProtocolVersion, Row }
        |
        |trait TupleRowDecoder {
        -
        -  implicit def tuple${arity}RowDecoder[${`A..N`}](implicit
        -    ${`a:F[A]..n:F[N]`("RowDeserializer")}
        -  ): RowDecoder[${`(A..N)`}] =
        -    new RowDecoder[${`(A..N)`}] {
        -      def apply(row: Row, ver: ProtocolVersion): Result[${`(A..N)`}] =
        -        ${tupled}
        -    }
        |}
      """
    }
  }

  object GenTupleBinder extends Template {
    def file(root: File) = root / "agni" / "TupleBinder.scala"
    def content(tv: TemplateVals) = {
      import tv._
      val expr = (synVals zipWithIndex) map { case (v,i) => s"$v(bound, $i, xs._${i + 1}, ver)" }
      val maped = s"(${expr mkString " >> "}).map(_ => bound)"
      block"""
        |package agni
        |
        |import cats.instances.either._
        |import cats.syntax.either._
        |import cats.syntax.flatMap._
        |import com.datastax.driver.core.{ BoundStatement, ProtocolVersion }
        |
        |trait TupleBinder {
        -
        -  implicit def tuple${arity}Binder[${`A..N`}](implicit
        -    ${`a:F[A]..n:F[N]`("RowSerializer")}
        -  ): Binder[${`(A..N)`}] =
        -    new Binder[${`(A..N)`}] {
        -      def apply(bound: BoundStatement, ver: ProtocolVersion, xs: ${`(A..N)`}): Result[BoundStatement] =
        -        ${maped}
        -    }
        |}
      """
    }
  }
}
