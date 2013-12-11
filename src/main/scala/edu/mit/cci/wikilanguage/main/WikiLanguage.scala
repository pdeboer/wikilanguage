package edu.mit.cci.wikilanguage.main

import java.util.Calendar
import java.text.SimpleDateFormat
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.wiki.{PersonDegreeProcessor, PersonLifetimeAnnotator, PersonLinkAnnotationProcessor}


/**
 * @author Patrick de Boer
 */
object WikiLanguage extends App {
	PersonDiscoverer.main(null)
	//PersonLinker.main(null)
	//PersonLinkAnnotator.main(null)
	//PersonLifetimeAnnotatorExec.main(null)

//	PersonDegreeProcessorApp.main(null)
}
