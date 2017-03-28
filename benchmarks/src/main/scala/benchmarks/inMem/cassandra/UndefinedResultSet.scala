package benchmarks.inMem.cassandra

import java.util

import com.datastax.driver.core._
import com.google.common.util.concurrent.ListenableFuture

trait UndefinedResultSet extends ResultSet {
  override def one(): Row = ???
  override def getColumnDefinitions: ColumnDefinitions = ???
  override def wasApplied(): Boolean = ???
  override def isExhausted: Boolean = ???
  override def all(): util.List[Row] = ???
  override def getExecutionInfo: ExecutionInfo = ???
  override def getAvailableWithoutFetching: Int = ???
  override def isFullyFetched: Boolean = ???
  override def iterator(): util.Iterator[Row] = ???
  override def getAllExecutionInfo: util.List[ExecutionInfo] = ???
  override def fetchMoreResults(): ListenableFuture[ResultSet] = ???
}
