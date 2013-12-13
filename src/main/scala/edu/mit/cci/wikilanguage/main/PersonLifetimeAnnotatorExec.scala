package edu.mit.cci.wikilanguage.main

import java.util.concurrent.Executors
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.{PersonDegreeProcessor, PersonLifetimeAnnotator, PersonLinkAnnotationProcessor, PersonLinkProcessor}

/**
 * @author pdeboer
 *         First created on 04/12/13 at 11:56
 */
object PersonLifetimeAnnotatorExec extends App {
	val exec = Executors.newFixedThreadPool(25)

	DAO.getAllPeopleIDs().foreach(id => {
		exec.submit(new Runnable {
			def run() {
				try {
					new PersonLifetimeAnnotator().processPerson(id)
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
