package agni

import org.scalatest.Assertion

class BinderSpec extends TypedSuite {
  import TypedSuite._

  def checkType[A: Binder]: Assertion = {
    assertCompiles("Binder.apply[A]")
  }

  test("Binder[Named]")(checkType[Named])
  test("Binder[IDV]")(checkType[IDV])

  test("Binder[T1]")(checkType[T1])
  test("Binder[T2]")(checkType[T2])
  test("Binder[T3]")(checkType[T3])
  test("Binder[T4]")(checkType[T4])
  test("Binder[T5]")(checkType[T5])
  test("Binder[T6]")(checkType[T6])
  test("Binder[T7]")(checkType[T7])
  test("Binder[T8]")(checkType[T8])
  test("Binder[T9]")(checkType[T9])
  test("Binder[T10]")(checkType[T10])
  test("Binder[T11]")(checkType[T11])
  test("Binder[T12]")(checkType[T12])
  test("Binder[T13]")(checkType[T13])
  test("Binder[T14]")(checkType[T14])
  test("Binder[T15]")(checkType[T15])
  test("Binder[T16]")(checkType[T16])
  test("Binder[T17]")(checkType[T17])
  test("Binder[T18]")(checkType[T18])
  test("Binder[T19]")(checkType[T19])
  test("Binder[T20]")(checkType[T20])
  test("Binder[T21]")(checkType[T21])
  test("Binder[T22]")(checkType[T22])
  test("Binder[T22_2]")(checkType[T22_2])
}
