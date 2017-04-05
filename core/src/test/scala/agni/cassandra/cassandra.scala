package agni

package object cassandra {

  def toNull[T]: T = null.asInstanceOf[T]
}
