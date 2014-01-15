package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.{TopNPeopleRetrieverByYear, BetweennessByYearCalculator}
import java.util.concurrent.Executors

/**
 * @author pdeboer
 *         First created on 14/01/14 at 18:45
 */
object BetweennessCalculator extends App {
	println("started..")

	val calculator = new BetweennessByYearCalculator()

	val exec = Executors.newFixedThreadPool(10)

	DAO.getYears().foreach(year => {
		exec.submit(new Runnable {
			def run() {
				try {
					calculator.process(year)
				}
				catch {
					case e: Exception => {
						println("couldnt process " + year)
						e.printStackTrace(System.err)
					}
				}
			}
		})
	})
	exec.shutdown()

	println("finished.")
}
