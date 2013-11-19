package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wiki.ArticleCache
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.model.Conversions._
import edu.mit.cci.util.U
import java.util.concurrent.{Executors, Callable, ExecutorService}
import scala.collection.JavaConversions.asScalaSet
import scala.collection.mutable
import java.util.Collections
import edu.mit.cci.wikilanguage.model.{Person, Category}

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:17 AM
 */

case class CategoriesAndPeople(categories: List[Category], people: List[Person])

class CategoryContentProcessor(cat: Category, insertDB: Boolean = true) extends Callable[CategoriesAndPeople] {
	//automatically retry content-fetching
	def categoryContents(maxTries: Int = 3): Array[String] = {
		if (maxTries == 0) return Array.empty[String]

		val wiki = new WikiCategory(cat.name, lang = cat.lang)
		if (wiki.contents.size == 0) categoryContents(maxTries - 1) else wiki.contents
	}

	def call(): CategoriesAndPeople = {

		val dao = new DAO()
		if (insertDB) dao.insertCategory(cat)

		var retCategories = List.empty[Category]
		var retPeople = List.empty[Person]

		categoryContents().foreach(c => {
			if (c.startsWith("Category:")) {
				if (isPersonCategory(c)) {
					// should be processed
					retCategories ::= Category(c, cat.lang)()
				}
			}
			else if (isPerson(c)) {
				try {
					val article = ArticleCache.get(c, cat.lang)
					if (insertDB) dao.insertPerson(article, resolveCategories = false)

					retPeople ::= Person(c, cat.lang)()
				}
				catch {
					case e: Throwable => e.printStackTrace()
				}
			}
		})

		println("finished analyzing category " + cat.name + " , found " + retCategories.length + " are to be processed")

		CategoriesAndPeople(retCategories, retPeople)
	}

	def isPerson(name: String) = !checkStringContains(name, Array(":", "list", "wikipedia"))

	def isPersonCategory(name: String): Boolean = {
		!checkStringContains(name, Array("cleanup")) &&
		  (U.containsNumber(name) || checkStringContains(name, Array("birth", "death")))
	}

	private def checkStringContains(str: String, contains: Array[String]): Boolean = {
		val lowerCaseString = str.toLowerCase()

		contains.filter(lowerCaseString.contains(_)).size > 0
	}
}