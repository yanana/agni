package agni.cassandra

import java.util

import com.datastax.driver.core.{ ColumnDefinitions, ExecutionInfo, ResultSet, Row }
import com.google.common.util.concurrent.ListenableFuture

import scala.collection.JavaConverters._

class AResultSet(rows: Seq[Row]) extends UndefinedResultSet {
  val head = rows.head
  override def one(): Row = head
  override def iterator(): util.Iterator[Row] = rows.asJava.iterator()
}
