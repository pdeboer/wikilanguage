package edu.mit.cci.wikilanguage.db

import edu.mit.cci.db.{Connector, DAOQueryReturningType}
import Connector.autoCloseStmt
import java.sql.ResultSet
import edu.mit.cci.wikilanguage.model.{Person, Category}

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:13 AM
 */
class DAO extends DAOQueryReturningType {


	def insertCategory(c: Category): Int = {
		try {
			val category = categoryByName(c.name)

			if (category != null) return category.id

			autoCloseStmt("INSERT INTO categories (name, wiki_language) VALUES (?,?) ") {
				stmt =>
					stmt.setString(1, c.name)
					stmt.setString(2, c.lang)
			}
			return categoryByName(c.name).id
		}
		catch {
			case e: Throwable => println("couldnt insert category " + c.name)
		}

		//just in case we got interrupted by another thread
		categoryByName(c.name).id
	}

	private def getCategoryWithDefaultResultSet(r: ResultSet) =
		new Category(r.getString(2), lang = r.getString(3))(id = r.getInt(1))

	def categoryByName(name: String): Category = {
		val data = typedQuery[Category](
			"SELECT id, name, wiki_language FROM categories WHERE name = ?",
			p => p.setString(1, name), r => getCategoryWithDefaultResultSet(r))

		if (data.size > 0) return data(0)
		else return null
	}

	def personByName(name: String, fetchCategories: Boolean = false): Person = {
		try {
			val data = typedQuery[Person](
				"SELECT id, name, wiki_language FROM people WHERE name = ?",
				p => p.setString(1, name), r => new Person(r.getString(2), lang = r.getString(3))(id = r.getInt(1)))

			val person = if (data.size > 0) data(0) else null

			if (fetchCategories && person != null) {
				enrichPersonWithCategories(person)
			}

			person
		}
		catch {
			case e:Throwable => null
		}
	}

	def personById(id: Int, fetchCategories: Boolean = false): Person = {
		val data = typedQuery[Person](
			"SELECT id, name, wiki_language FROM people WHERE id = ?",
			p => p.setInt(1, id), r => new Person(r.getString(2), lang = r.getString(3))(id = r.getInt(1)))

		val person = if (data.size > 0) data(0) else null

		if (fetchCategories) {
			enrichPersonWithCategories(person)
		}

		person
	}

	private def enrichPersonWithCategories(person: Person) {
		if (person != null) {
			val categories = typedQuery[Category](
				"""
          SELECT c.id, c.name, c.wiki_language
          FROM categories c INNER JOIN people2categories p2c ON c.id = p2c.category
          WHERE c.id = ?
				""", p => p.setInt(1, person.id), r => getCategoryWithDefaultResultSet(r))
			person.categories ++= categories
		}
	}


	def insertPerson(a: Person, resolveCategories:Boolean = false): Int = {
		try {
			val person = personByName(a.name)
			if (person != null) return person.id

			autoCloseStmt("INSERT INTO people (name, wiki_language) VALUES (?,?)") {
				stmt =>
					stmt.setString(1, a.name)
					stmt.setString(2, a.lang)
			}

			val personId = personByName(a.name).id

			if (resolveCategories && a.categories != null) {
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
		catch {
			case e: Throwable => println("couldnt insert person " + a.name)
		}

		//just in case we got interrupted by another thread
		personByName(a.name).id
	}

	def insertPeopleConnectionID(fromPersonId: Int, toPersonName: String, articleId: Int, lang: String): Boolean = {
		try {
			autoCloseStmt(
				"""INSERT INTO connections (person_from, person_to, article_name, wiki_language)
        SELECT ?, id, ?, ? FROM people WHERE name = ?
				""") {
				stmt =>
					stmt.setInt(1, fromPersonId)
					stmt.setInt(2, articleId)
					stmt.setString(3, lang)
					stmt.setString(4, toPersonName)
			}
			true
		}
		catch {
			case e: Throwable => println("couldnt insert connection " + fromPersonId + " to " + toPersonName)
				false
		}
	}

	def getAllPeopleIDs(): List[Int] = {
		typedQuery[Int]("SELECT id FROM people", s => {}, r => r.getInt(1))
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
}
