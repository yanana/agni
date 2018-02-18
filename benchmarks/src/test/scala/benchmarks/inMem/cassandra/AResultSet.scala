package benchmarks.inMem.cassandra

import com.datastax.driver.core.Row

import scala.collection.JavaConverters._

class AResultSet(testRows: Seq[Row]) extends UndefinedResultSet {
  private[this] val head = testRows.head
  private[this] val rows = testRows.asJava

  override def one(): Row = head
  override def iterator(): java.util.Iterator[Row] = rows.iterator()
}
