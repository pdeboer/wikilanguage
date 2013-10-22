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
import edu.mit.cci.wikilanguage.model.Category

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:17 AM
 */
class CategoryProcessor(val lang: String = "en") {
	private val processed: mutable.Set[String] = new java.util.HashSet[String]()
	private val exec = Executors.newFixedThreadPool(40)

	def shutdown() { exec.shutdown() }

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
			addToQueue(new CategoryContentProcessor(category).call())
		}
	}

}

class CategoryContentProcessor(category: Category) extends Callable[List[Category]] {
	def call(): List[Category] = {
		val cat = new WikiCategory(category.name, lang = category.lang)
		val dao = new DAO()

		dao.insertCategory(cat)

		var ret = List.empty[Category]

		cat.contents.foreach(c => {
			if (c.startsWith("Category:")) {
				if (isPersonCategory(c)) {
					// should be processed
					ret ::= Category(c, category.lang)()
				}
			}
			else if (isPerson(c)) {
				try {
					val article = ArticleCache.get(c, category.lang)
					dao.insertPerson(article)
				}
				catch {
					case e: Exception => e.printStackTrace()
				}
			}
		})

		println("finished analyzing category " + cat.name + ", found " + cat.contents.length + " children of which " + ret.length + " are to be processed")

		ret
	}

	def isPerson(name: String) = checkString(name, Array(":", "list", "wikipedia"))

	def isPersonCategory(name: String): Boolean =
		checkString(name, Array("cleanup")) && U.containsNumber(name)

	private def checkString(str: String, notContains: Array[String]): Boolean = {
		val lowerCaseString = str.toLowerCase()

		notContains.forall(!lowerCaseString.contains(_))
	}
}