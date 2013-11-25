package edu.mit.cci.db

import java.sql.{ResultSet, PreparedStatement}
import Connector.autoClose

/**
 * @author pdeboer
 *         First created on 9/12/13 at 3:23 PM
 */
trait DAOQueryReturningType {
  def typedQuery[T](query: String,
                    setParams: ((PreparedStatement) => Unit),
                    assembleListObject: ((ResultSet) => T)): List[T] = {
    autoClose(Connector.getConnection) {
      conn =>
        if (conn != null) {
          autoClose(conn.prepareStatement(query)) {
            stmt =>
              setParams(stmt)

              autoClose(stmt.executeQuery()) {
                rs =>

                  var res: List[T] = Nil

                  while (rs.next()) {
                    try {
						res ::= assembleListObject(rs)
					}
					catch {
						case e:Throwable => e.printStackTrace()
					}
                  }

                  return res
              }
          }
        }
    }
    return Nil
  }
}