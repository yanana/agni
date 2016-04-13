import java.nio.ByteBuffer

import com.datastax.driver.core._

object codec {

  class LongToObjectCodec extends TypeCodec[Object](DataType.bigint(), classOf[Object]) {
    def serialize(t: Object, protocolVersion: ProtocolVersion): ByteBuffer =
      TypeCodec.bigint().serialize(
        t.asInstanceOf[java.lang.Long],
        protocolVersion
      )
    def parse(s: String): Object = s.toLong.asInstanceOf[Object]
    def format(t: Object): String = t.toString
    def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Object = {
      val i = TypeCodec.bigint().deserialize(bytes, protocolVersion)
      if (i == null) null else i.asInstanceOf[Object]
    }
  }

  class AsciiToObjectCodec extends TypeCodec[Object](DataType.ascii(), classOf[Object]) {
    def serialize(t: Object, protocolVersion: ProtocolVersion): ByteBuffer =
      TypeCodec.ascii().serialize(
        t.asInstanceOf[java.lang.String],
        protocolVersion
      )
    def parse(s: String): Object = s.toLong.asInstanceOf[Object]
    def format(t: Object): String = t.toString
    def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Object = {
      val i = TypeCodec.ascii().deserialize(bytes, protocolVersion)
      if (i == null) null else i.asInstanceOf[Object]
    }
  }

}
