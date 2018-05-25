package benchmarks.inMem

import java.nio.ByteBuffer

import benchmarks.inMem.cassandra.UndefinedRow
import com.datastax.driver.core.{ ProtocolVersion, Row, TypeCodec }

class Base {

  val v: ProtocolVersion = ProtocolVersion.NEWEST_BETA

  val row: Row = new UndefinedRow {
    override def getString(i: Int): String = "fff"
    override def getString(name: String): String = "ggg"

    override def getBytesUnsafe(i: Int): ByteBuffer = TypeCodec.ascii().serialize("fff", v)
    override def getBytesUnsafe(name: String): ByteBuffer = TypeCodec.ascii().serialize("ggg", v)
  }
}
