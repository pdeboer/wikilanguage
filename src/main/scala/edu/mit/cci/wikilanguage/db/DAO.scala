package edu.mit.cci.wikilanguage.db

import edu.mit.cci.db.{Connector, DAOQueryReturningType}
import Connector.autoCloseStmt
import Connector.insertReturnID
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
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import edu.mit.cci.wikilanguage.util.LRUCacheFactory

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:13 AM
 */
object DAO extends DAOQueryReturningType {
	def insertCategory(c: Category): Int = {
		try {
			val category = categoryByName(c.name)

			if (category == null) {
				insertReturnID("INSERT INTO categories (name, wiki_language) VALUES (?,?) ") {
					stmt =>
						stmt.setString(1, c.name)
						stmt.setString(2, c.lang)
				}.toInt
			} else category.id
		}
		catch {
			case e: Throwable => {
				println("couldnt insert category " + c.name);
				-1
			}
		}
	}

	private def getCategoryWithDefaultResultSet(r: ResultSet) =
		new Category(r.getString(2), lang = r.getString(3))(id = r.getInt(1))

	def categoryByName(name: String): Category = {
		val data = typedQuery[Category](
			"SELECT id, name, wiki_language FROM categories WHERE name = ?",
			p => p.setString(1, name), r => getCategoryWithDefaultResultSet(r))

		if (data.size > 0) data(0) else null
	}

	def personByName(name: String, fetchCategories: Boolean = false): Person = {
		val cache = LRUCacheFactory.get[Person]("PERSONBYNAME")
		val cacheAnswer: Person = cache.get(name)
		if (cacheAnswer != null) {
			return cacheAnswer
		}

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

			cache.put(name, person)

			person
		}
		catch {
			case e: Throwable => {
				e.printStackTrace()
				null
			}
		}
	}

	def personById(id: Int, fetchCategories: Boolean = false): Person = {
		val cache = LRUCacheFactory.get[Person]("PERSONBYID")
		val cacheAnswer: Person = cache.get(id + "")
		if (cacheAnswer != null) {
			return cacheAnswer
		}

		try {
			val data = typedQuery[Person](
				"SELECT id, name, wiki_language FROM people WHERE id = ?",
				p => p.setInt(1, id), r => new Person(r.getString(2), lang = r.getString(3))(id = r.getInt(1)))

			val person = if (data.size > 0) data(0) else null

			if (fetchCategories) {
				enrichPersonWithCategories(person)
			}
			cache.put(id + "", person)

			person
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

	def peopleIdsWithoutRedirection() = {
		typedQuery[Int]("SELECT id FROM people WHERE id NOT IN (SELECT target_person FROM people_redirections)",
			c=>null, _.getInt(1))
	}

	def processPersonMeta(a: Person, resolveCategories: Boolean = false, resolveRedirects:Boolean = false) {
		synchronized {
			val personId = a.id

			//add content if necessary
			if (a.content != null && a.content != "") {
				autoCloseStmt("DELETE FROM peoplecontent WHERE id = ?") {
					stmt => stmt.setInt(1, personId)
				}

				autoCloseStmt("INSERT INTO peoplecontent (id, content) VALUES (?,?)") {
					stmt =>
						stmt.setInt(1, personId)
						stmt.setString(2, a.content)
				}
			}

			if (resolveCategories && a.categories != null && a.categories.size > 0) {
				//delete previous categories
				autoCloseStmt("DELETE FROM people2categories WHERE person = ?") {
					stmt => stmt.setInt(1, personId)
				}

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

			if(resolveRedirects && a.synonyms != null && a.synonyms.length > 0) {
				//delete previous categories
				autoCloseStmt("DELETE FROM people_redirections WHERE target_person = ?") {
					stmt => stmt.setInt(1, personId)
				}

				//insert synonyms
				a.synonyms.foreach(c => {
					autoCloseStmt("INSERT INTO people_redirections (title, target_person) VALUES (?,?)") {
						stmt =>
							stmt.setString(1, c)
							stmt.setInt(2, personId)
					}
				})
			}
		}
	}

	def insertPerson(a: Person): Int = {
		synchronized {
			try {
				val person = personByName(a.name)
				if (person != null) return person.id

				val personId = insertReturnID("INSERT INTO people (name, wiki_language) VALUES (?,?)") {
					stmt =>
						stmt.setString(1, a.name)
						stmt.setString(2, a.lang)
				}.toInt

				return personId
			}
			catch {
				case e: Throwable => {
					if (U.exceptionHasCase[com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException](e)) {
						e.printStackTrace()
						println("couldnt insert person " + a.name)
					}
				}
			}
			-1
		}
	}

	def insertPeopleConnectionID(fromPersonId: Int, toPersonName: String, articleId: Int, lang: String): Boolean = {
		try {
			//direct connection
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

			//aliases
			autoCloseStmt(
				"""
				INSERT INTO connections (person_from, person_to, article_name, wiki_language)
        		SELECT ?, target_person, ?, ? FROM people_redirections p WHERE title = ? AND target_person NOT IN (
					SELECT person_to FROM connections where person_to = p.target_person AND person_from = ?
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
			  		(SELECT count(distinct person_from) FROM connections WHERE person_to = ?) AS indeg,
					(SELECT count(distinct person_to) FROM connections WHERE person_from =?) AS outdeg,
			  		(SELECT count(distinct person_from) FROM connections WHERE person_to =? AND NOT year_from IS NULL) AS indeg_alive,
			  		(SELECT count(distinct person_to) FROM connections WHERE person_from =? AND NOT year_from IS NULL) AS outdeg_alive
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

	def storeTopIndegreePersonInt(experiment: Experiment, indegree: Int) = {
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

	def storeTopIndegreePersonDouble(experiment: Experiment, data: Double) = {
		try {
			autoCloseStmt("INSERT INTO year_people_experiments (person_id, year_id, experiment_name, dataDouble) VALUES(?,?,?,?)") {
				stmt =>
					stmt.setInt(1, experiment.person)
					stmt.setInt(2, experiment.year)
					stmt.setString(3, experiment.name)
					stmt.setDouble(4, data)
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
			SELECT p.id, indegree_alive FROM people p
			  	INNER JOIN people_aux a ON p.id = a.id AND ? BETWEEN p.year_from AND p.year_to AND p.year_from IS NOT NULL
			ORDER BY a.indegree_alive DESC
			LIMIT ?""",
			s => {
				s.setInt(1, year)
				s.setInt(2, limit)
			}, r => PersonDegree(r.getInt(1), r.getInt(2)))
		p
	}

	case class PersonAux(personId: Int, indegree: Int, outdegree: Int, numChars: Int, indegreeAlive: Int, outdegreeAlive: Int, pageRank: Double = 0d)

	def getPersonAux(person: Int) = {
		val p = typedQuery[PersonAux]( """
		  SELECT id, indegree, outdegree, num_chars, indegree_alive, outdegree_alive, pagerank FROM people_aux WHERE id = ?
									   """,
			_.setInt(1, person), o => PersonAux(o.getInt(1), o.getInt(2), o.getInt(3), o.getInt(4), o.getInt(5), o.getInt(6), o.getDouble(7)))

		if (p.size > 0) p(0) else null
	}

	def getAllPeopleAux() = {
		typedQuery[PersonAux]( """
		  SELECT id, indegree, outdegree, num_chars,
		  	indegree_alive, outdegree_alive, pagerank
		  FROM people_aux""",
			l => {}, o => PersonAux(o.getInt(1), o.getInt(2), o.getInt(3), o.getInt(4), o.getInt(5), o.getInt(6), o.getDouble(7)))
	}

	def getPopularPeopleByYearByIndegreeAndArticleSize(year: Int, limit: Int = 5) = {
		val p = typedQuery[PersonDegree](
			"""
			SELECT p.id, indegree_alive FROM people p
			  	INNER JOIN people_aux a ON p.id = a.id AND ? BETWEEN p.year_from AND p.year_to AND p.year_from IS NOT NULL
			ORDER BY a.indegree_alive*a.num_chars/4621+ a.indegree_alive/a.outdegree_alive DESC
			LIMIT ?""",
			s => {
				s.setInt(1, year)
				s.setInt(2, limit)
			}, r => PersonDegree(r.getInt(1), r.getInt(2)))
		p
	}

	def getConnectionsByYear(year: Int) = {
		val p = typedQuery[PersonLink](
			"""
			  SELECT id, person_from, person_to FROM connections WHERE ? BETWEEN year_from AND IFNULL(year_to, year_from+100) AND year_FROM IS NOT NULL
			""", s => s.setInt(1, year), r => PersonLink(r.getInt(1), r.getInt(2), r.getInt(3))
		)
		p
	}

	def getPeopleWithGivenDeathYear(year: Int) = typedQuery[Int]("SELECT id FROM people WHERE year_to=?", _.setInt(1, year), _.getInt(1))

	def getAlivePeople() = typedQuery[Int]("SELECT id FROM people WHERE IFNULL(year_to, year_to+100) > YEAR(NOW())", r => {}, _.getInt(1))

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

	def removeExperimentsInYear(name: String, year: Int) {
		autoCloseStmt("DELETE FROM year_people_experiments WHERE experiment_name=? AND year_id=?") {
			stmt =>
				stmt.setString(1, name)
				stmt.setInt(2, year)
		}
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
