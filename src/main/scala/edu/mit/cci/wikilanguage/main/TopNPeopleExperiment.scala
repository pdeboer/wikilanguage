package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.io.GraphVizExporter
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.TopNPeopleRetrieverByYear

/**
 * @author pdeboer
 *         First created on 16/12/13 at 13:12
 */
object TopNPeopleExperiment extends App {
	val retriever = new TopNPeopleRetrieverByYear()
	DAO.getYears().par.foreach(retriever.process(_))
}
