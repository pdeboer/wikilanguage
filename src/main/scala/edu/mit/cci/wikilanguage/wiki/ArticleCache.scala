package edu.mit.cci.wiki

import edu.mit.cci.model.active.WikiArticle
import scala.collection.mutable
import java.util
import java.util.Map.Entry

/**
 * @author pdeboer
 *         First created on 8/15/13 at 4:58 PM
 */
object ArticleCache {
	private val MAX_SIZE = 1000

	private val articles = new util.LinkedHashMap[String, WikiArticle](MAX_SIZE + 1, 1.1f, true) {
		override def removeEldestEntry(eldest: Entry[String, WikiArticle]): Boolean = size() > MAX_SIZE
	}

	def get(title: String, lang:String="en"): WikiArticle = {
		articles.synchronized({
			if (!articles.containsKey(title)) {
				articles.put(title, new WikiArticle(title, lang))
			}

			articles.get(title)
		})
	}
}
