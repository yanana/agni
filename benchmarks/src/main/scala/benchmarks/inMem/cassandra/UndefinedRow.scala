package benchmarks.inMem.cassandra

import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util
import java.util.{ Date, UUID }

import com.datastax.driver.core._
import com.google.common.reflect.TypeToken

trait UndefinedRow extends Row {
  override def getPartitionKeyToken: Token = ???
  override def getColumnDefinitions: ColumnDefinitions = ???
  override def getToken(i: Int): Token = ???
  override def getToken(name: String): Token = ???
  override def getByte(i: Int): Byte = ???
  override def getTime(i: Int): Long = ???
  override def getTupleValue(i: Int): TupleValue = ???
  override def getDouble(i: Int): Double = ???
  override def getInet(i: Int): InetAddress = ???
  override def getFloat(i: Int): Float = ???
  override def getUDTValue(i: Int): UDTValue = ???
  override def getBytes(i: Int): ByteBuffer = ???
  override def getUUID(i: Int): UUID = ???
  override def getBytesUnsafe(i: Int): ByteBuffer = ???
  override def getTimestamp(i: Int): Date = ???
  override def getList[T](i: Int, elementsClass: Class[T]): util.List[T] = ???
  override def getList[T](i: Int, elementsType: TypeToken[T]): util.List[T] = ???
  override def get[T](i: Int, targetClass: Class[T]): T = ???
  override def get[T](i: Int, targetType: TypeToken[T]): T = ???
  override def get[T](i: Int, codec: TypeCodec[T]): T = ???
  override def getDate(i: Int): LocalDate = ???
  override def getBool(i: Int): Boolean = ???
  override def getDecimal(i: Int): java.math.BigDecimal = ???
  override def getVarint(i: Int): BigInteger = ???
  override def getObject(i: Int): AnyRef = ???
  override def getSet[T](i: Int, elementsClass: Class[T]): util.Set[T] = ???
  override def getSet[T](i: Int, elementsType: TypeToken[T]): util.Set[T] = ???
  override def getShort(i: Int): Short = ???
  override def getString(i: Int): String = ???
  override def getMap[K, V](i: Int, keysClass: Class[K], valuesClass: Class[V]): util.Map[K, V] = ???
  override def getMap[K, V](i: Int, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = ???
  override def getLong(i: Int): Long = ???
  override def getInt(i: Int): Int = ???
  override def isNull(i: Int): Boolean = ???
  override def getByte(name: String): Byte = ???
  override def getTime(name: String): Long = ???
  override def getTupleValue(name: String): TupleValue = ???
  override def getDouble(name: String): Double = ???
  override def getInet(name: String): InetAddress = ???
  override def getFloat(name: String): Float = ???
  override def getUDTValue(name: String): UDTValue = ???
  override def getBytes(name: String): ByteBuffer = ???
  override def getUUID(name: String): UUID = ???
  override def getBytesUnsafe(name: String): ByteBuffer = ???
  override def getTimestamp(name: String): Date = ???
  override def getList[T](name: String, elementsClass: Class[T]): util.List[T] = ???
  override def getList[T](name: String, elementsType: TypeToken[T]): util.List[T] = ???
  override def get[T](name: String, targetClass: Class[T]): T = ???
  override def get[T](name: String, targetType: TypeToken[T]): T = ???
  override def get[T](name: String, codec: TypeCodec[T]): T = ???
  override def getDate(name: String): LocalDate = ???
  override def getBool(name: String): Boolean = ???
  override def getDecimal(name: String): java.math.BigDecimal = ???
  override def getVarint(name: String): BigInteger = ???
  override def getObject(name: String): AnyRef = ???
  override def getSet[T](name: String, elementsClass: Class[T]): util.Set[T] = ???
  override def getSet[T](name: String, elementsType: TypeToken[T]): util.Set[T] = ???
  override def getShort(name: String): Short = ???
  override def getString(name: String): String = ???
  override def getMap[K, V](name: String, keysClass: Class[K], valuesClass: Class[V]): util.Map[K, V] = ???
  override def getMap[K, V](name: String, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = ???
  override def getLong(name: String): Long = ???
  override def getInt(name: String): Int = ???
  override def isNull(name: String): Boolean = ???
}
