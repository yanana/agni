package agni.cassandra

import java.lang.reflect.Constructor
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util
import java.util.{ Collections, Date, UUID }

import agni.FakeUDTValue
import com.datastax.driver.core.DataType.Name
import com.datastax.driver.core.UserType.Field
import com.datastax.driver.core._
import com.google.common.reflect.TypeToken

case class ARow(
    time: Long = 0L,
    double: Double = 0.0,
    inet: InetAddress = InetAddress.getByName("127.0.0.1"),
    float: Float = 0.0f,
    bytes: ByteBuffer = ByteBuffer.allocate(0),
    uuid: UUID = UUID.randomUUID(),
    unsafeBytes: ByteBuffer = ByteBuffer.allocateDirect(0),
    timestamp: Date = new Date,
    date: LocalDate = LocalDate.fromMillisSinceEpoch(System.currentTimeMillis()),
    bool: Boolean = true,
    decimal: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    varint: java.math.BigInteger = java.math.BigInteger.ZERO,
    short: Short = 0.toShort,
    string: String = "",
    long: Long = 0L,
    int: Int = 0,
    byte: Byte = 0.toByte
) extends UndefinedRow {

  override def getTime(i: Int): Long = time
  override def getTime(name: String): Long = time

  override def getDouble(i: Int): Double = double
  override def getDouble(name: String): Double = double

  override def getInet(i: Int): InetAddress = inet
  override def getInet(name: String): InetAddress = inet

  override def getFloat(i: Int): Float = float
  override def getFloat(name: String): Float = float

  override def getBytes(i: Int): ByteBuffer = bytes
  override def getBytes(name: String): ByteBuffer = bytes

  override def getUUID(i: Int): UUID = uuid
  override def getUUID(name: String): UUID = uuid

  override def getBytesUnsafe(i: Int): ByteBuffer = unsafeBytes
  override def getBytesUnsafe(name: String): ByteBuffer = unsafeBytes

  override def getTimestamp(i: Int): Date = timestamp
  override def getTimestamp(name: String): Date = timestamp

  override def getList[T](i: Int, elementsType: TypeToken[T]): util.List[T] = new util.ArrayList()
  override def getList[T](name: String, elementsType: TypeToken[T]): util.List[T] = new util.ArrayList()

  override def getDate(i: Int): LocalDate = date
  override def getDate(name: String): LocalDate = date

  override def getBool(i: Int): Boolean = bool
  override def getBool(name: String): Boolean = bool

  override def getDecimal(i: Int): java.math.BigDecimal = decimal
  override def getDecimal(name: String): java.math.BigDecimal = decimal

  override def getVarint(i: Int): BigInteger = varint
  override def getVarint(name: String): BigInteger = varint

  override def getSet[T](i: Int, elementsType: TypeToken[T]): util.Set[T] = new util.HashSet()
  override def getSet[T](name: String, elementsType: TypeToken[T]): util.Set[T] = new util.HashSet()

  override def getShort(i: Int): Short = short
  override def getShort(name: String): Short = short

  override def getString(i: Int): String = string
  override def getString(name: String): String = string

  override def getMap[K, V](i: Int, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = new util.HashMap()
  override def getMap[K, V](name: String, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = new util.HashMap()

  override def getLong(i: Int): Long = long
  override def getLong(name: String): Long = long

  override def getInt(i: Int): Int = int
  override def getInt(name: String): Int = int

  override def getByte(i: Int): Byte = byte
  override def getByte(name: String): Byte = byte

  override def isNull(i: Int): Boolean = false
  override def isNull(name: String): Boolean = false
}

class ANullRow extends UndefinedRow {
  override def isNull(name: String): Boolean = true
  override def getPartitionKeyToken: Token = toNull
  override def getColumnDefinitions: ColumnDefinitions = toNull
  override def getToken(i: Int): Token = toNull
  override def getToken(name: String): Token = toNull
  override def getByte(i: Int): Byte = toNull
  override def getTime(i: Int): Long = toNull
  override def getTupleValue(i: Int): TupleValue = toNull
  override def getDouble(i: Int): Double = toNull
  override def getInet(i: Int): InetAddress = toNull
  override def getFloat(i: Int): Float = toNull
  override def getUDTValue(i: Int): UDTValue = toNull
  override def getBytes(i: Int): ByteBuffer = toNull
  override def getUUID(i: Int): UUID = toNull
  override def getBytesUnsafe(i: Int): ByteBuffer = toNull
  override def getTimestamp(i: Int): Date = toNull
  override def getList[T](i: Int, elementsClass: Class[T]): util.List[T] = toNull
  override def getList[T](i: Int, elementsType: TypeToken[T]): util.List[T] = toNull
  override def get[T](i: Int, targetClass: Class[T]): T = null.asInstanceOf[T]
  override def get[T](i: Int, targetType: TypeToken[T]): T = null.asInstanceOf[T]
  override def get[T](i: Int, codec: TypeCodec[T]): T = null.asInstanceOf[T]
  override def getDate(i: Int): LocalDate = toNull
  override def getBool(i: Int): Boolean = toNull
  override def getDecimal(i: Int): java.math.BigDecimal = toNull
  override def getVarint(i: Int): BigInteger = toNull
  override def getObject(i: Int): AnyRef = toNull
  override def getSet[T](i: Int, elementsClass: Class[T]): util.Set[T] = toNull
  override def getSet[T](i: Int, elementsType: TypeToken[T]): util.Set[T] = toNull
  override def getShort(i: Int): Short = toNull
  override def getString(i: Int): String = toNull
  override def getMap[K, V](i: Int, keysClass: Class[K], valuesClass: Class[V]): util.Map[K, V] = toNull
  override def getMap[K, V](i: Int, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = toNull
  override def getLong(i: Int): Long = toNull
  override def getInt(i: Int): Int = toNull
  override def isNull(i: Int): Boolean = toNull
  override def getByte(name: String): Byte = toNull
  override def getTime(name: String): Long = toNull
  override def getTupleValue(name: String): TupleValue = toNull
  override def getDouble(name: String): Double = toNull
  override def getInet(name: String): InetAddress = toNull
  override def getFloat(name: String): Float = toNull
  override def getUDTValue(name: String): UDTValue = toNull
  override def getBytes(name: String): ByteBuffer = toNull
  override def getUUID(name: String): UUID = toNull
  override def getBytesUnsafe(name: String): ByteBuffer = toNull
  override def getTimestamp(name: String): Date = toNull
  override def getList[T](name: String, elementsClass: Class[T]): util.List[T] = toNull
  override def getList[T](name: String, elementsType: TypeToken[T]): util.List[T] = toNull
  override def get[T](name: String, targetClass: Class[T]): T = null.asInstanceOf[T]
  override def get[T](name: String, targetType: TypeToken[T]): T = null.asInstanceOf[T]
  override def get[T](name: String, codec: TypeCodec[T]): T = null.asInstanceOf[T]
  override def getDate(name: String): LocalDate = toNull
  override def getBool(name: String): Boolean = toNull
  override def getDecimal(name: String): java.math.BigDecimal = toNull
  override def getVarint(name: String): BigInteger = toNull
  override def getObject(name: String): AnyRef = toNull
  override def getSet[T](name: String, elementsClass: Class[T]): util.Set[T] = toNull
  override def getSet[T](name: String, elementsType: TypeToken[T]): util.Set[T] = toNull
  override def getShort(name: String): Short = toNull
  override def getString(name: String): String = toNull
  override def getMap[K, V](name: String, keysClass: Class[K], valuesClass: Class[V]): util.Map[K, V] = toNull
  override def getMap[K, V](name: String, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = toNull
  override def getLong(name: String): Long = toNull
  override def getInt(name: String): Int = toNull
}

class EmptyRow extends UndefinedRow {

  override def getTime(i: Int): Long = 0L
  override def getTime(name: String): Long = 0L

  override def getDouble(i: Int): Double = 0.0
  override def getDouble(name: String): Double = 0.0

  override def getInet(i: Int): InetAddress = InetAddress.getByName("127.0.0.1")
  override def getInet(name: String): InetAddress = InetAddress.getByName("127.0.0.1")

  override def getFloat(i: Int): Float = 0.0f
  override def getFloat(name: String): Float = 0.0f

  override def getBytes(i: Int): ByteBuffer = ByteBuffer.allocate(0)
  override def getBytes(name: String): ByteBuffer = ByteBuffer.allocate(0)

  override def getUUID(i: Int): UUID = UUID.randomUUID()
  override def getUUID(name: String): UUID = UUID.randomUUID()

  override def getBytesUnsafe(i: Int): ByteBuffer = ByteBuffer.allocateDirect(0)
  override def getBytesUnsafe(name: String): ByteBuffer = ByteBuffer.allocateDirect(0)

  override def getTimestamp(i: Int): Date = new Date()
  override def getTimestamp(name: String): Date = new Date()

  override def getList[T](i: Int, elementsType: TypeToken[T]): util.List[T] = new util.ArrayList()
  override def getList[T](name: String, elementsType: TypeToken[T]): util.List[T] = new util.ArrayList()

  override def getDate(i: Int): LocalDate = LocalDate.fromMillisSinceEpoch(0)
  override def getDate(name: String): LocalDate = LocalDate.fromMillisSinceEpoch(0)

  override def getBool(i: Int): Boolean = true
  override def getBool(name: String): Boolean = true

  override def getDecimal(i: Int): java.math.BigDecimal = java.math.BigDecimal.ZERO
  override def getDecimal(name: String): java.math.BigDecimal = java.math.BigDecimal.ZERO

  override def getVarint(i: Int): BigInteger = BigInteger.ZERO
  override def getVarint(name: String): BigInteger = BigInteger.ZERO

  override def getSet[T](i: Int, elementsType: TypeToken[T]): util.Set[T] = new util.HashSet()
  override def getSet[T](name: String, elementsType: TypeToken[T]): util.Set[T] = new util.HashSet()

  override def getShort(i: Int): Short = 0.toShort
  override def getShort(name: String): Short = 0.toShort

  override def getString(i: Int): String = ""
  override def getString(name: String): String = ""

  override def getMap[K, V](i: Int, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = new util.HashMap()
  override def getMap[K, V](name: String, keysType: TypeToken[K], valuesType: TypeToken[V]): util.Map[K, V] = new util.HashMap()

  override def getLong(i: Int): Long = 0L
  override def getLong(name: String): Long = 0L

  override def getInt(i: Int): Int = 0
  override def getInt(name: String): Int = 0

  override def getByte(i: Int): Byte = 0.toByte
  override def getByte(name: String): Byte = 0.toByte

  override def getToken(i: Int): Token = new AToken
  override def getToken(name: String): Token = new AToken

  private[this] def tupleValue: TupleValue = {
    val c = classOf[TupleValue]
    val cc = c.getDeclaredConstructor(classOf[TupleType])
    cc.setAccessible(true)
    cc.newInstance(TupleType.of(ProtocolVersion.V3, CodecRegistry.DEFAULT_INSTANCE, DataType.ascii()))
  }
  override def getTupleValue(i: Int): TupleValue = tupleValue
  override def getTupleValue(name: String): TupleValue = tupleValue

  override def getUDTValue(i: Int): UDTValue = FakeUDTValue.get()
  override def getUDTValue(name: String): UDTValue = FakeUDTValue.get()

  override def isNull(i: Int): Boolean = false
  override def isNull(name: String): Boolean = false
}
