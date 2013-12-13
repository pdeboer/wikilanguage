package edu.mit.cci.wikilanguage.main

import java.util.concurrent.Executors
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.{PersonArticleSizeProcessor, PersonDegreeProcessor, PersonLinkAnnotationProcessor}

/**
 * @author pdeboer
 *         First created on 09/12/13 at 11:29
 */
object PersonAuxProcessor extends App{
	val exec = Executors.newFixedThreadPool(50)

	DAO.createPeopleAuxEntries()
	val articleTextProcessor = new PersonArticleSizeProcessor()
	DAO.getAllPeopleIDsWithKnownBirthdate().foreach(id => {
		exec.submit(new Runnable {
			def run() {
				try {
					new PersonDegreeProcessor().process(id)
					articleTextProcessor.process(id)
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
