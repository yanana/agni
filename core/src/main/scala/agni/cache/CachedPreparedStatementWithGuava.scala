package agni
package cache

import java.util.concurrent.Callable

import com.datastax.driver.core.{ PreparedStatement, RegularStatement, Session }
import com.google.common.cache.Cache

trait CachedPreparedStatementWithGuava extends CachedPreparedStatement[Cache] {

  protected val cache: Cache[String, PreparedStatement]

  override def getPrepared(session: Session, stmt: RegularStatement): PreparedStatement =
    cache.get(stmt.toString, new Callable[PreparedStatement] {
      override def call(): PreparedStatement = session.prepare(stmt)
    })

  override def clear(): Unit = cache.cleanUp()
}
