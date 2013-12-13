package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wiki.ArticleCache

/**
 * @author pdeboer
 *         First created on 13/12/13 at 11:09
 */
class PersonArticleSizeProcessor {
	def getSize(personId:Int) = {
		val person = DAO.personById(personId)
		val article = ArticleCache.get(personId, person.name, person.lang)

		article.parsed.text().size
	}
}
