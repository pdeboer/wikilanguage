package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.model.active.WikiArticle
import edu.mit.cci.util.U
import org.jsoup.nodes.Element

/**
 * User: pdeboer
 * Date: 10/17/13
 * Time: 10:47 PM
 */
class PersonLinkProcessor(val personId: Int) {
	val dao = new DAO()
	val person = dao.personById(personId)

	def urlStart = Array("http://" + person.lang + ".wikipedia.org/wiki/", "/wiki/", "/w/")

	/**
	 * insert link into database to all people that already exist in database
	 * which this person is referencing
	 */
	def process() {
		println("started processing of " + personId)
		outlinks.foreach(l =>
			dao.insertPeopleConnectionID(person.id, l, person.id, person.lang))
		println("ended processing of " + personId)
	}

	/**
	 * find names of all articles this article is referencing
	 * @return
	 */
	def outlinks(): List[String] = {
		try {
			val article = new WikiArticle(person.name, person.lang)

			//fetch all outgoing links of article and return their names
			val linkElements = U.convertJSoupToList(article.parsed.select("a"))
			val cleanedList = linkElements.filter(a => loc(a) != null && loc(a).length > 0).map(loc(_))
			val links = cleanedList.map(l => {
				val u = urlStart.find(l.startsWith(_))

				if(!u.isEmpty) l.substring(u.get.length)
				else null
			})

			links.filterNot(_==null)
		}
		catch {
			case e: Exception => {
				println("couldnt parse " + person.name)
				List.empty[String]
			}
		}

	}

	def likelyPersonOutlinks():List[String] = {
		outlinks.filter(u => !u.contains(":") && !u.startsWith("index.php"))
	}

	private def loc(e:Element):String = e.attr("href")

}
