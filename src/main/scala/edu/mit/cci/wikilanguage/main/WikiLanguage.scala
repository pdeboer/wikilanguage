package edu.mit.cci.wikilanguage.main

import java.util.Calendar
import java.text.SimpleDateFormat
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.wiki.{PersonLinkProcessor, PersonDegreeProcessor, PersonLifetimeAnnotator, PersonLinkAnnotationProcessor}
import edu.mit.cci.wikilanguage.db.DAO


/**
 * @author Patrick de Boer
 */
object WikiLanguage extends App {
	//PersonDiscoverer.main(null)
	//PersonMetaProcessor.main(null)
	//PersonLinker.main(null)
	//PersonLinkAnnotator.main(null)
	//PersonLifetimeAnnotatorExec.main(null)
	//PersonAuxProcessor.main(null)
	//GraphVizExporter.main(null)
		//TopNPeopleExperiment.main(null)
	//	EdgeListExporter.main(null)
	BetweennessCalculator.main(null)
	//WekaExporterMain.main(null)
}
