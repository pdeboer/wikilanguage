package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.db.DAO
import java.util.concurrent.Executors
import edu.mit.cci.wikilanguage.wiki.PersonLinkProcessor

/**
 * User: pdeboer
 * Date: 10/17/13
 * Time: 10:47 PM
 */
object PersonLinker extends App {
	val exec = Executors.newFixedThreadPool(50)

	val dao = new DAO()
	dao.getAllPeopleIDs().foreach(id => {
		exec.submit(new Runnable {
			def run() {
				try {
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
