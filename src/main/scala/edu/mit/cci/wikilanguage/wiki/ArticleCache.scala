package edu.mit.cci.wiki

import scala.collection.mutable
import java.util
import java.util.Map.Entry
import edu.mit.cci.wikilanguage.model.active.WikiArticle
import edu.mit.cci.wikilanguage.db.DAO
import scala.ref.WeakReference

/**
 * @author pdeboer
 *         First created on 8/15/13 at 4:58 PM
 */
object ArticleCache {
	private val MAX_SIZE = 10000

	private val articles = new util.LinkedHashMap[String, WeakReference[WikiArticle]](MAX_SIZE + 1, 1.1f, true) {
		override def removeEldestEntry(eldest: Entry[String, WeakReference[WikiArticle]]): Boolean = size() > MAX_SIZE
	}

	/**
	 * get wiki article from cache
	 * @param title
	 * @param lang
	 * @return
	 */
	def get(title: String, lang: String = "en"): WikiArticle = {
		articles.synchronized({
			if (!articles.containsKey(title) || articles.get(title).get.isEmpty) {
				articles.put(title, new WeakReference[WikiArticle](new WikiArticle(title, lang)))
			}

			articles.get(title).get.get
		})
	}

	/**
	 * get wiki article from cache. if available, try fetching content from DB
	 * @param id
	 * @param title
	 * @param lang
	 * @return
	 */
	def get(id: Int, title: String, lang: String): WikiArticle = {
		val article = get(title,lang)
		if(article.text == "") {
			val contentDB: String = DAO.personContentById(id)
			article.text = if(contentDB == null) "" else contentDB
		}
		article
	}


}
