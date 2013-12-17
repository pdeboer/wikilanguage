package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.io.FileExporter
import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 16/12/13 at 13:12
 */
object EdgeListExporter extends App {
	val exporter = new FileExporter()
	exporter.init(DAO.getAllPeopleIDsWithBirthdateAndIndegreeGt(2), printNames = true)
	println("exporting..")
	exporter.exportEdgeList("edgelist.csv")
	println("all set.")
}
