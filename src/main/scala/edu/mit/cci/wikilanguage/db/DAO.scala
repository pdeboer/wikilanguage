package edu.mit.cci.wikilanguage.db

import edu.mit.cci.db.{Connector, DAOQueryReturningType}
import Connector.autoCloseStmt
import java.sql.ResultSet
import edu.mit.cci.wikilanguage.model.{Person, Category}
import java.util.Date
import java.sql
import java.text.SimpleDateFormat
import edu.mit.cci.util.U
import scala.Predef._
import scala.Some
import edu.mit.cci.wikilanguage.model.Category
import edu.mit.cci.wikilanguage.model.Person

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:13 AM
 */
object DAO extends DAOQueryReturningType {
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
			type n = (String) => Integer
			val number: n = (s: String) => if (s != null) s.toInt else null

			val data = typedQuery[Person](
				"SELECT id, name, wiki_language, year_from, year_to FROM people WHERE name = ?",
				p => p.setString(1, U.entityEscape(name)), r => new Person(r.getString(2), lang = r.getString(3))(id = r.getInt(1),
					yearFrom = number(r.getString(4)), yearTo = number(r.getString(5))))

			val person = if (data.size > 0) data(0) else null

			if (fetchCategories && person != null) {
				enrichPersonWithCategories(person)
			}

			return person
		}
		catch {
			case e: Throwable => {
				e.printStackTrace()
				return null
			}
		}
	}

	def personById(id: Int, fetchCategories: Boolean = false): Person = {
		try {
			val data = typedQuery[Person](
				"SELECT id, name, wiki_language FROM people WHERE id = ?",
				p => p.setInt(1, id), r => new Person(r.getString(2), lang = r.getString(3))(id = r.getInt(1)))

			val person = if (data.size > 0) data(0) else null

			if (fetchCategories) {
				enrichPersonWithCategories(person)
			}
			return person
		}
		catch {
			case e: Throwable => return null
		}

	}

	private def enrichPersonWithCategories(person: Person) {
		if (person != null) {
			val categories = typedQuery[Category](
				"""
          SELECT c.id, c.name, c.wiki_language
          FROM categories c INNER JOIN people2categories p2c ON c.id = p2c.category
          WHERE p2c.person = ?
				""", p => p.setInt(1, person.id), r => getCategoryWithDefaultResultSet(r))
			person.categories ++= categories
		}
	}

	def personContentById(id: Int): String = {
		val c = typedQuery[String](
			"""
			  SELECT content FROM peoplecontent WHERE id = ?
			""", _.setInt(1, id), _.getString(1))

		if (c != null && c.size > 0) c(0) else null
	}


	def insertPerson(a: Person, resolveCategories: Boolean = false): Int = {
		try {
			val person = personByName(a.name)
			if (person != null) return person.id

			autoCloseStmt("INSERT INTO people (name, wiki_language) VALUES (?,?)") {
				stmt =>
					stmt.setString(1, a.name)
					stmt.setString(2, a.lang)
			}

			val personId = personByName(a.name).id

			//add content if necessary
			if (a.content != null && a.content != "") {
				autoCloseStmt("INSERT INTO peoplecontent (id, content) VALUES (?,?)") {
					stmt =>
						stmt.setInt(1, personId)
						stmt.setString(2, a.content)
				}
			}

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
			case e: Throwable => {
				e.printStackTrace()
				println("couldnt insert person " + a.name)
			}
		}

		//just in case we got interrupted by another thread
		personByName(a.name).id
	}

	def insertPeopleConnectionID(fromPersonId: Int, toPersonName: String, articleId: Int, lang: String): Boolean = {
		try {
			autoCloseStmt(
				"""
				INSERT INTO connections (person_from, person_to, article_name, wiki_language)
        		SELECT ?, id, ?, ? FROM people p WHERE name = ? AND id NOT IN (
					SELECT person_to FROM connections where person_to = p.id AND person_from = ?
        		)
				""") {
				stmt =>
					stmt.setInt(1, fromPersonId)
					stmt.setInt(2, articleId)
					stmt.setString(3, lang)
					stmt.setString(4, toPersonName)
					stmt.setInt(5, fromPersonId)
			}
			true
		}
		catch {
			case e: Throwable => println("couldnt insert connection " + fromPersonId + " to " + toPersonName + ": " + e.getMessage)
				false
		}
	}

	def updatePeopleConnection(connectionId: Int, fromDate: Date, toDate: Date): Boolean = {
		try {
			val year = (d: Date) => {
				if (d == null) None
				else {
					val mul = if (new SimpleDateFormat("G").format(d) == "BC") -1 else 1

					Some("" + (new SimpleDateFormat("yyyy").format(d).toInt * mul))
				}
			}

			autoCloseStmt("UPDATE connections SET year_from=?, year_to=? WHERE id=?") {
				stmt =>
					stmt.setString(1, year(fromDate).getOrElse(null))
					stmt.setString(2, year(toDate).getOrElse(null))
					stmt.setInt(3, connectionId)
			}
			true
		}
		catch {
			case e: Throwable => {
				e.printStackTrace()
				println("couldnt update connection " + connectionId)
				false
			}
		}
	}

	def updatePersonYears(personId: Int, fromDate: Date, toDate: Date): Boolean = {
		try {
			val year = (d: Date) => {
				if (d == null) None
				else {
					val mul = if (new SimpleDateFormat("G").format(d) == "BC") -1 else 1

					Some("" + (new SimpleDateFormat("yyyy").format(d).toInt * mul))
				}
			}

			autoCloseStmt("UPDATE people SET year_from=?, year_to=? WHERE id=?") {
				stmt =>
					stmt.setString(1, year(fromDate).getOrElse(null))
					stmt.setString(2, year(toDate).getOrElse(null))
					stmt.setInt(3, personId)
			}
			true
		}
		catch {
			case e: Throwable => {
				e.printStackTrace()
				println("couldnt update person " + personId)
				false
			}
		}
	}

	def getPersonDegrees(personId: Int): PersonDegrees = {
		val p = typedQuery[PersonDegrees](
			"""SELECT
					(SELECT count(distinct person_to) FROM connections WHERE person_from =?) AS outdeg,
			  		(SELECT count(distinct person_from) FROM connections WHERE person_to = ?) AS indeg,
			  		(SELECT count(distinct person_to) FROM connections WHERE person_from =? AND NOT year_from IS NULL) AS indeg_alive,
			  		(SELECT count(distinct person_from) FROM connections WHERE person_to =? AND NOT year_from IS NULL) AS outdeg_alive
			""", s => {
				s.setInt(1, personId)
				s.setInt(2, personId)
				s.setInt(3, personId)
				s.setInt(4, personId)
			}, o => PersonDegrees(o.getInt(1), o.getInt(2), o.getInt(3), o.getInt(4)))

		if (p.size > 0) p(0) else null
	}

	case class PersonDegrees(indegree: Int, outdegree: Int, indegreeAlive: Int, outdegreeAlive: Int)

	def storePersonDegrees(personId: Int, degree: PersonDegrees, numChars: Int) = {
		try {
			autoCloseStmt("INSERT INTO people_aux (id, indegree, outdegree, num_chars, indegree_alive, outdegree_alive) VALUES(?,?,?,?,?,?)") {
				stmt =>
					stmt.setInt(1, personId)
					stmt.setInt(2, degree.indegree)
					stmt.setInt(3, degree.outdegree)
					stmt.setInt(4, numChars)
					stmt.setInt(5, degree.indegreeAlive)
					stmt.setInt(6, degree.outdegreeAlive)

			}
			true
		}
		catch {
			case e: Throwable => {
				e.printStackTrace()
				println("couldnt add person degree to " + personId)
				false
			}
		}
	}

	case class Experiment(name: String, person: Int = -1, year: Int)

	def storeTopIndegreePerson(experiment: Experiment, indegree: Int) = {
		try {
			autoCloseStmt("INSERT INTO year_people_experiments (person_id, year_id, experiment_name, dataInt) VALUES(?,?,?,?)") {
				stmt =>
					stmt.setInt(1, experiment.person)
					stmt.setInt(2, experiment.year)
					stmt.setString(3, experiment.name)
					stmt.setInt(4, indegree)
			}
			true
		}
		catch {
			case e: Throwable => {
				e.printStackTrace()
				println("couldnt add person experiment " + experiment)
				false
			}
		}
	}

	def cleanPeopleAuxEntries() {
		autoCloseStmt("TRUNCATE people_aux") {
			stmt => null
		}
	}

	case class PersonDegree(personId: Int, degree: Int)

	def getPopularPeopleByYearByIndegree(year: Int, limit: Int = 5) = {
		val p = typedQuery[PersonDegree](
			"""
			SELECT p.id, indegree FROM people p
			  	INNER JOIN people_aux a ON p.id = a.id AND ? BETWEEN p.year_from AND p.year_to AND p.year_from IS NOT NULL
			ORDER BY a.indegree DESC
			LIMIT ?""",
			s => {
				s.setInt(1, year)
				s.setInt(2, limit)
			}, r => PersonDegree(r.getInt(1), r.getInt(2)))
		p
	}

	def getPersonOutlinks(sourcePersonId: Int): List[PersonLink] = {
		val p = typedQuery[PersonLink]("SELECT id, person_to FROM connections WHERE person_from = ?",
			_.setInt(1, sourcePersonId), o => PersonLink(o.getInt(1), sourcePersonId, o.getInt(2)))
		p
	}

	def getPersonInlinks(targetPersonId: Int): List[PersonLink] = {
		val p = typedQuery[PersonLink]("SELECT id, person_from FROM connections WHERE person_to = ?",
			_.setInt(1, targetPersonId), o => PersonLink(o.getInt(1), o.getInt(2), targetPersonId))
		p
	}

	case class PersonLink(id: Int, personFromId: Int, personToId: Int)

	def getAllPeopleIDs(): List[Int] = {
		typedQuery[Int]("SELECT id FROM people", s => {}, r => r.getInt(1))
	}

	def getAllPeopleIDsWithKnownBirthdate(): List[Int] = {
		typedQuery[Int]("SELECT id FROM people WHERE year_from IS NOT NULL", s => {}, r => r.getInt(1))
	}

	def getAllPeopleIDsWithBirthdateAndIndegreeGt(minIndegree: Int): List[Int] = {
		typedQuery[Int]( """
		  SELECT p.id FROM people p INNER JOIN people_aux a ON p.id = a.id
		  WHERE year_from IS NOT NULL AND a.indegree_alive > ?
						 """, s => s.setInt(1, minIndegree), r => r.getInt(1))
	}


	def getYears(): List[Int] = {
		typedQuery[Int]("SELECT id FROM years", s => {}, r => r.getInt(1))
	}


	def truncateConnections() {
		autoCloseStmt("TRUNCATE connections") {
			stmt => null
		}
	}

	def addPersonYearEstimations() {
		autoCloseStmt("update people set year_from = year_to - 100 where year_from is null") {
			stmt => null
		}

		autoCloseStmt("update people set year_to = year_from + 100 where year_to is null") {
			stmt => null
		}
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
		truncateConnections()
		autoCloseStmt("TRUNCATE peoplecontent") {
			stmt => null
		}
	}
}
