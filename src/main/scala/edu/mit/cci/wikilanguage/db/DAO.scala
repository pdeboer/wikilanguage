package edu.mit.cci.wikilanguage.db

import edu.mit.cci.wikilanguage.model.WikiCategory
import edu.mit.cci.db.{Connector, DAOQueryReturningType}
import Connector.autoClose
import Connector.autoCloseStmt
import java.sql.PreparedStatement
import edu.mit.cci.model.active.WikiArticle

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:13 AM
 */
class DAO extends DAOQueryReturningType {
  def insertCategory(c: WikiCategory): Int = {
    val category = categoryByName(c.category)

    if (category != null) return category.id

    autoCloseStmt("INSERT INTO categories (name, wiki_language) VALUES (?,?) ") {
      stmt =>
        stmt.setString(1, c.category)
        stmt.setString(2, c.lang)

        stmt.execute()
    }

    return categoryByName(c.category).id
  }

  def categoryByName(name: String): WikiCategory = {
    val data = typedQuery[WikiCategory](
      "SELECT id, name, wiki_language FROM categories WHERE name = ?",
      p => p.setString(1, name), r => new WikiCategory(r.getString(2), lang = r.getString(3), id = r.getInt(1)))

    if (data.size > 0) return data(0)
    else return null
  }

  def personByName(name: String): WikiArticle = {
    val data = typedQuery[WikiArticle](
      "SELECT id, name, wiki_language FROM people WHERE name = ?",
      p => p.setString(1, name), r => new WikiArticle(r.getString(2), lang = r.getString(3), id = r.getInt(1)))

    if (data.size > 0) return data(0)
    else return null
  }

  def insertPerson(a: WikiArticle): Int = {
    val person = personByName(a.name)
    if (person != null) return person.id

    a.categories //get categories to make sure they have been retrieved

    autoCloseStmt("INSERT INTO people (name, wiki_language) VALUES (?,?)") {
      stmt =>
        stmt.setString(1, a.name)
        stmt.setString(2, a.lang)
    }

    val personId = personByName(a.name).id

    //insert super-categories
    a.categories.foreach(c => {
      val categoryId = insertCategory(c) //make sure category exists and get id

      autoCloseStmt("INSERT INTO people2categories (person, category) VALUES (?,?)") {
        stmt =>
          stmt.setInt(1, personId)
          stmt.setInt(2, categoryId)
      }
    })
    return personId
  }


  //TODO code
  private def lock(name: String): Object = {
    null
  }
}
