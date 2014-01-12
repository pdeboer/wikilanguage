package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.io.{PeopleAuxWeka, WekaExporter}
import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 12/01/14 at 21:46
 */
object WekaExporterMain extends App {
	println("getting data")
	val we = new WekaExporter(DAO.getAllPeopleAux().map(p =>
		PeopleAuxWeka(p.indegreeAlive, p.outdegreeAlive, p.numChars, p.pageRank, "")))
	println("got data, exporting..")
	we.export("wekatest.arff")
	println("finished")
}
