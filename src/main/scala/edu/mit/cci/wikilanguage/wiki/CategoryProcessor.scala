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
class CategoryProcessor(val lang: String = "en") {
	private val processed: mutable.Set[String] = new java.util.HashSet[String]()
	private val exec = Executors.newFixedThreadPool(40)

	def shutdown() {
		exec.shutdown()
	}

	def setProcessed(category: Category) {
		synchronized {
			processed += category.name
		}
	}

	def isProcessed(category: Category): Boolean = synchronized {
		return processed.contains(category.name)
	}

	def addToQueue(elements: List[Category]) {
		elements.foreach(addToQueue(_))
	}

	def addToQueue(element: Category) {
		synchronized {
			if (!isProcessed(element)) {
				processed += element.name
				exec.execute(new Worker(element))
			}
		}
	}

	def process(name: String) {
		addToQueue(Category(name, lang)())
	}

	private class Worker(category: Category) extends Runnable {
		def run() {
			addToQueue(new CategoryContentProcessor(category).call().categories)
		}
	}

}

case class CategoriesAndPeople(categories: List[Category], people: List[Person])

class CategoryContentProcessor(cat: Category, insertDB: Boolean = true) extends Callable[CategoriesAndPeople] {
	//automatically retry content-fetching
	def categoryContents(maxTries: Int = 3): Array[String] = {
		if (maxTries == 0) return Array.empty[String]

		val wiki = new WikiCategory(cat.name, lang = cat.lang)
		if (wiki.contents.size == 0) categoryContents(maxTries - 1) else wiki.contents
	}

	def call(): CategoriesAndPeople = {
		if (cat.name.equals("Category:1944_births")) println("@1944category") //TODO remove

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
					if(c.contains("Ledergerber")) println("Ledergerber found") //TODO remove
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