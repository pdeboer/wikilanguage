package edu.mit.cci.wikilanguage.db

import edu.mit.cci.db.{Connector, DAOQueryReturningType}
import Connector.autoClose
import Connector.autoCloseStmt
import java.sql.PreparedStatement
import edu.mit.cci.wikilanguage.model.active.{WikiArticle, WikiCategory}
import edu.mit.cci.wikilanguage.model.{Person, Category}

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:13 AM
 */
class DAO extends DAOQueryReturningType {
  def insertCategory(c: Category): Int = {
    val category = categoryByName(c.name)

    if (category != null) return category.id

    autoCloseStmt("INSERT INTO categories (name, wiki_language) VALUES (?,?) ") {
      stmt =>
        stmt.setString(1, c.name)
        stmt.setString(2, c.lang)
    }

    return categoryByName(c.name).id
  }

  def categoryByName(name: String): Category = {
    val data = typedQuery[Category](
      "SELECT id, name, wiki_language FROM categories WHERE name = ?",
      p => p.setString(1, name), r => new Category(r.getString(2), lang = r.getString(3), id = r.getInt(1)))

    if (data.size > 0) return data(0)
    else return null
  }

  def personByName(name: String): Person = {
    val data = typedQuery[Person](
      "SELECT id, name, wiki_language FROM people WHERE name = ?",
      p => p.setString(1, name), r => new Person(r.getString(2), lang = r.getString(3), id = r.getInt(1)))

    if (data.size > 0) return data(0)
    else return null
  }

  def insertPerson(a: Person): Int = {
    val person = personByName(a.name)
    if (person != null) return person.id

    autoCloseStmt("INSERT INTO people (name, wiki_language) VALUES (?,?)") {
      stmt =>
        stmt.setString(1, a.name)
        stmt.setString(2, a.lang)
    }

    val personId = personByName(a.name).id

    if (a.categories != null) {
      //insert super-categories
      a.categories.foreach(c => {
        val categoryId = insertCategory(c) //make sure category exists and get id

        autoCloseStmt("INSERT INTO people2categories (person, category) VALUES (?,?)") {
          stmt =>
            stmt.setInt(1, personId)
            stmt.setInt(2, categoryId)
        }
      })
    }
    return personId
  }

  def clean() {
    autoCloseStmt("TRUNCATE people") {
      stmt => null
    }
    autoCloseStmt("TRUNCATE people2categories") {
      stmt => null
    }
    autoCloseStmt("TRUNCATE categories") {
      stmt => null
    }
    autoCloseStmt("TRUNCATE connections") {
      stmt => null
    }
  }


  //TODO code
  private def lock(name: String): Object = {
    null
  }
}
