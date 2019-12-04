package benchmarks.inMem.cassandra

import java.nio.ByteBuffer

import com.datastax.oss.driver.api.core.{ CqlIdentifier, ProtocolVersion }
import com.datastax.oss.driver.api.core.`type`.DataType
import com.datastax.oss.driver.api.core.`type`.codec.registry.CodecRegistry
import com.datastax.oss.driver.api.core.cql.{ ColumnDefinitions, Row }
import com.datastax.oss.driver.api.core.detach.AttachmentPoint

class UndefinedRow extends Row {
  override def getColumnDefinitions: ColumnDefinitions = ???

  override def isDetached: Boolean = ???

  override def attach(attachmentPoint: AttachmentPoint): Unit = ???

  override def firstIndexOf(id: CqlIdentifier): Int = ???

  override def getType(id: CqlIdentifier): DataType = ???

  override def firstIndexOf(name: String): Int = ???

  override def getType(name: String): DataType = ???

  override def getBytesUnsafe(i: Int): ByteBuffer = ???

  override def size(): Int = ???

  override def getType(i: Int): DataType = ???

  override def codecRegistry(): CodecRegistry = ???

  override def protocolVersion(): ProtocolVersion = ???
}
