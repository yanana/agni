package agni

import com.datastax.driver.core.{ PreparedStatement, Session }
import com.google.common.cache.{ Cache, CacheBuilder, CacheLoader, LoadingCache }

import scala.collection.concurrent.TrieMap

package object cache {

  object loading {

    def sizedCache(size: Long, session: Session): LoadingCache[String, PreparedStatement] =
      CacheBuilder.newBuilder()
        .maximumSize(size)
        .build[String, PreparedStatement](new CacheLoader[String, PreparedStatement] {
          override def load(key: String): PreparedStatement = session.prepare(key)
        })
  }

  object default {

    implicit lazy val cache: Cache[String, PreparedStatement] =
      CacheBuilder.newBuilder().build[String, PreparedStatement]
  }

  object trie {

    implicit lazy val cache: TrieMap[String, PreparedStatement] = TrieMap.empty
  }
}
