package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.model.active.WikiArticle
import edu.mit.cci.util.U
import org.jsoup.nodes.Element
import edu.mit.cci.wiki.ArticleCache

/**
 * User: pdeboer
 * Date: 10/17/13
 * Time: 10:47 PM
 */
class PersonLinkProcessor(val personId: Int) {
	/**
	 * insert link into database to all people that already exist in database
	 * which this person is referencing
	 */
	def process() {
		println("started processing of " + personId)
		val person = DAO.personById(personId)
		val article = ArticleCache.get(personId, person.name, person.lang)
		article.likelyPersonOutlinks().foreach(l =>
			DAO.insertPeopleConnectionID(person.id, l, person.id, person.lang))
		new PersonLinkAnnotationProcessor().processPerson(person.id) //also annotate links
		println("ended processing of " + personId)
	}



}
