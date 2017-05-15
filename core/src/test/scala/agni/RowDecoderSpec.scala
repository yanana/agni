package agni

import org.scalatest.Assertion

class RowDecoderSpec extends TypedSuite {

  def checkType[A: RowDecoder]: Assertion = {
    assertCompiles("RowDecoder.apply[A]")
  }

  test("RowDecoder[Named]")(checkType[Named])
  test("RowDecoder[IDV]")(checkType[IDV])

  test("RowDecoder[T1]")(checkType[T1])
  test("RowDecoder[T2]")(checkType[T2])
  test("RowDecoder[T3]")(checkType[T3])
  test("RowDecoder[T4]")(checkType[T4])
  test("RowDecoder[T5]")(checkType[T5])
  test("RowDecoder[T6]")(checkType[T6])
  test("RowDecoder[T7]")(checkType[T7])
  test("RowDecoder[T8]")(checkType[T8])
  test("RowDecoder[T9]")(checkType[T9])
  test("RowDecoder[T10]")(checkType[T10])
  test("RowDecoder[T11]")(checkType[T11])
  test("RowDecoder[T12]")(checkType[T12])
  test("RowDecoder[T13]")(checkType[T13])
  test("RowDecoder[T14]")(checkType[T14])
  test("RowDecoder[T15]")(checkType[T15])
  test("RowDecoder[T16]")(checkType[T16])
  test("RowDecoder[T17]")(checkType[T17])
  test("RowDecoder[T18]")(checkType[T18])
  test("RowDecoder[T19]")(checkType[T19])
  test("RowDecoder[T20]")(checkType[T20])
  test("RowDecoder[T21]")(checkType[T21])
  test("RowDecoder[T22]")(checkType[T22])
  test("RowDecoder[T22_2]")(checkType[T22_2])
}
