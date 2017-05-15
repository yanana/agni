package object agni {

  type Result[+R] = Either[Throwable, R]
}
