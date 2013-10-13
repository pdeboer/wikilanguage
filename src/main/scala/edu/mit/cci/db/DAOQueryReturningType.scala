package edu.mit.cci.db

import java.sql.{ResultSet, PreparedStatement}

/**
 * @author pdeboer
 *         First created on 9/12/13 at 3:23 PM
 */
trait DAOQueryReturningType {
	def typedQuery[T](query: String,
				 setParams: ((PreparedStatement) => Unit),
				 assembleListObject: ((ResultSet) => T)): List[T] = {
		val conn = Connector.getConnection
		if (conn != null) {
			val stmt = conn.prepareStatement(
				query)
			setParams(stmt)

			val rs = stmt.executeQuery()

			var res: List[T] = Nil

			if (rs.next()) {
				res ::= assembleListObject(rs)
			}

			res
		}
		Nil
	}
}