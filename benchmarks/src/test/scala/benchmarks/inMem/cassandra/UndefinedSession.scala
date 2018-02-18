package benchmarks.inMem.cassandra

import java.util

import com.datastax.driver.core._
import com.google.common.util.concurrent.ListenableFuture

class UndefinedSession extends Session {
  override def prepare(query: String): PreparedStatement = ???
  override def prepare(statement: RegularStatement): PreparedStatement = ???
  override def init(): Session = ???
  override def prepareAsync(query: String): ListenableFuture[PreparedStatement] = ???
  override def prepareAsync(statement: RegularStatement): ListenableFuture[PreparedStatement] = ???
  override def getLoggedKeyspace: String = ???
  override def getCluster: Cluster = ???
  override def execute(query: String): ResultSet = ???
  override def execute(query: String, values: AnyRef*): ResultSet = ???
  override def execute(query: String, values: util.Map[String, AnyRef]): ResultSet = ???
  override def execute(statement: Statement): ResultSet = ???
  override def initAsync(): ListenableFuture[Session] = ???
  override def getState: Session.State = ???
  override def isClosed: Boolean = ???
  override def closeAsync(): CloseFuture = ???
  override def executeAsync(query: String): ResultSetFuture = ???
  override def executeAsync(query: String, values: AnyRef*): ResultSetFuture = ???
  override def executeAsync(query: String, values: util.Map[String, AnyRef]): ResultSetFuture = ???
  override def executeAsync(statement: Statement): ResultSetFuture = ???
  override def close(): Unit = ()
}
