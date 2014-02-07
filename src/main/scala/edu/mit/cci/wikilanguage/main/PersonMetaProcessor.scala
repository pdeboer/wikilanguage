package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.db.DAO
import java.util.concurrent.Executors
import edu.mit.cci.wiki.ArticleCache
import edu.mit.cci.wikilanguage.model.{Person, Conversions}

/**
 * User: pdeboer
 * Date: 10/17/13
 * Time: 10:47 PM
 */
object PersonMetaProcessor extends App {
	val exec = Executors.newFixedThreadPool(25)

	DAO.peopleIdsWithoutRedirection().foreach(id => {
		exec.submit(new Runnable {
			def run() {
				try {
					val personDB = DAO.personById(id)
					val article = ArticleCache.get(id, personDB.name, personDB.lang)
					article.textFetched //make sure text is fetched

					val person: Person = Conversions.WikiArticle2Person(article)
					person.id = id

					DAO.processPersonMeta(person, true, true)

					println("processed "+id)
				}
				catch {
					case e: Exception => {
						println("couldnt process " + id)
						e.printStackTrace(System.err)
					}
				}
			}
		})
	})
	exec.shutdown()
}
