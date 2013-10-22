package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.model.active.WikiArticle
import edu.mit.cci.util.U

/**
 * User: pdeboer
 * Date: 10/17/13
 * Time: 10:47 PM
 */
class PersonLinkProcessor(val personId: Int) {
  val dao = new DAO()
  val person = dao.personById(personId)

  def urlStart = "http://" + person.lang + ".wikipedia.org/wiki/"

  /**
   * insert link into database to all people that already exist in database
   * which this person is referencing
   */
  def process() {
    outlinks.foreach(l => dao.insertPeopleConnectionID(person.id, l, person.id, person.lang))
  }

  /**
   * find names of all articles this article is referencing
   * @return
   */
  def outlinks(): List[String] = {
    try {
      val article = new WikiArticle(person.name, person.lang)

      //fetch all outgoing links of article and return their names
      U.convertJSoupToList(article.parsed.select("a")).filter(a => {
        val href = a.attr("href")
        href != null && href.length > 0 && href.startsWith(urlStart)
      }).map(a => a.attr("href").substring(urlStart.length))
    }
    catch {
      case e: Exception => {
        println("couldnt parse " + person.name)
        List.empty[String]
      }
    }

  }

}
