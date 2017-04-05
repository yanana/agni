package agni

import com.datastax.driver.core.{ PreparedStatement, RegularStatement, Session }

trait GetPreparedStatement {

  protected def getPrepared(session: Session, stmt: RegularStatement): PreparedStatement
}
