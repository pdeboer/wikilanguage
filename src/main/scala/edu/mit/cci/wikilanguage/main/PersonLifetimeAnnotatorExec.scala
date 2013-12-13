package edu.mit.cci.wikilanguage.main

import java.util.concurrent.{Semaphore, Executors}
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.{PersonDegreeProcessor, PersonLifetimeAnnotator, PersonLinkAnnotationProcessor, PersonLinkProcessor}

/**
 * @author pdeboer
 *         First created on 04/12/13 at 11:56
 */
object PersonLifetimeAnnotatorExec extends App {
	val exec = Executors.newFixedThreadPool(25)

	val ids = DAO.getAllPeopleIDs()
	val idSem = new Semaphore(-1 * ids.size + 1)

	ids.foreach(id => {
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
				} finally {
					idSem.release()
				}
			}
		})
	})
	exec.shutdown()

	//wait for processing to complete
	idSem.acquire()
	DAO.addPersonYearEstimations()
}
