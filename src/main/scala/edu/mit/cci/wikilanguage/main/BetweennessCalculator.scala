package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.BetweennessByYearCalculator

/**
 * @author pdeboer
 *         First created on 14/01/14 at 18:45
 */
object BetweennessCalculator extends App {
	println("started..")
	DAO.getYears().par.foreach(new BetweennessByYearCalculator().process(_))
	println("finished.")
}
