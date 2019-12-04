package benchmarks.inMem

import java.nio.ByteBuffer

import benchmarks.inMem.cassandra.UndefinedRow
import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodecs
import com.datastax.oss.driver.api.core.cql.Row

class Base {

  val v: ProtocolVersion = ProtocolVersion.DEFAULT

  val row: Row = new UndefinedRow {
    override def getString(i: Int): String = "fff"
    override def getString(name: String): String = "ggg"

    override def getBytesUnsafe(i: Int): ByteBuffer = TypeCodecs.ASCII.encode("fff", v)
    override def getBytesUnsafe(name: String): ByteBuffer = TypeCodecs.ASCII.encode("ggg", v)
  }
}
