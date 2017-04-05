package agni
package twitter.util

import agni.cache.CachedPreparedStatementWithGuava
import com.datastax.driver.core.PreparedStatement
import com.google.common.cache.Cache
import com.twitter.util.{ Try => TTry }
import io.catbird.util._

abstract class Try(implicit _cache: Cache[String, PreparedStatement])
    extends Agni[TTry, Throwable] with CachedPreparedStatementWithGuava {

  protected val cache: Cache[String, PreparedStatement] = _cache
}
