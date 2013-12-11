package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.db.DAO
import java.util.concurrent.Executors
import edu.mit.cci.wikilanguage.wiki.{PersonLifetimeAnnotator, PersonLinkProcessor}
import java.util.Random

/**
 * User: pdeboer
 * Date: 10/17/13
 * Time: 10:47 PM
 */
object PersonLinker extends App {
	val exec = Executors.newFixedThreadPool(50)

	DAO.truncateConnections()
	DAO.getAllPeopleIDs().foreach(id => {
		exec.submit(new Runnable {
			def run() {
				try {
					//delay between 0 and 100 seconds
					//Thread.sleep((new Random().nextDouble() * 100000).asInstanceOf[Long])

					new PersonLinkProcessor(id).process()
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
