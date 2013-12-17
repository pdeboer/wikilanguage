package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.io.FileExporter
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.{PersonLinkAnnotationProcessor, TopNPeopleRetrieverByYear}
import java.util.concurrent.Executors

/**
 * @author pdeboer
 *         First created on 16/12/13 at 13:12
 */
object TopNPeopleExperiment extends App {
	val retriever = new TopNPeopleRetrieverByYear()

	val exec = Executors.newFixedThreadPool(50)

	DAO.getYears().foreach(id => {
		exec.submit(new Runnable {
			def run() {
				try {
					retriever.process(id)
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
