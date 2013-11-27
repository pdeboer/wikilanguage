package edu.mit.cci.wikilanguage.model

import edu.mit.cci.wikilanguage.model.active.{WikiCategory, WikiArticle}
import edu.mit.cci.util.U

/**
 * User: pdeboer
 * Date: 10/16/13
 * Time: 6:12 PM
 */

case class Person(private val tempName: String, lang: String = "en")(var id: Int = -1, var categories: Array[Category] = Array.empty[Category], val content: String = null) {
	val name = U.wikiUnify(tempName)
}

case class Category(private val tempName: String, lang: String = "en")(var id: Int = -1) {
	val name = U.wikiUnify(tempName)
}

object Conversions {
	implicit def WikiArticle2Person(article: WikiArticle) =
		new Person(article.name, article.lang)(categories = Array.empty[Category] ++ article.categories.map(WikiCategory2Category(_)), content = article.text)

	implicit def WikiCategory2Category(category: WikiCategory) = new Category(category.category, category.lang)()

}