package agni.cassandra

import java.nio.ByteBuffer

import com.datastax.driver.core.{ DataType, ProtocolVersion, Token }

class AToken extends Token {
  override def getValue: AnyRef = ""
  override def serialize(protocolVersion: ProtocolVersion): ByteBuffer = ByteBuffer.allocate(0)
  override def getType: DataType = DataType.ascii()
  override def compareTo(o: Token): Int = 0
}
