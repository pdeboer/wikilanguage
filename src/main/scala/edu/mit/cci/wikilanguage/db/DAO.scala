package edu.mit.cci.wikilanguage.db

import edu.mit.cci.wikilanguage.model.WikiCategory
import edu.mit.cci.db.{Connector, DAOQueryReturningType}
import Connector.autoClose
import Connector.autoCloseStmt

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:13 AM
 */
class DAO extends DAOQueryReturningType {
  def insertCategory(c:WikiCategory):Boolean = {
    val data = typedQuery[String](
      "SELECT name FROM categories WHERE name = ?",
      p => p.setString(1, c.category), r => r.getString(1))
    //if category didnt exist yet
    if(data.size == 0) {
      //TODO insert
    }
    return true
  }
}
