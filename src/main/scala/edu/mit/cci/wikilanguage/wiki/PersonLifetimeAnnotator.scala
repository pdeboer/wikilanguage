package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 06/12/13 at 18:20
 */
class PersonLifetimeAnnotator {
	def processPerson(id: Int) {
		val source = DAO.personById(id, true)
		val fromTo = new PersonLinkTimestampDeterminer(source).determine
		if (fromTo.from != null || fromTo.to != null)
			DAO.updatePersonYears(source.id, fromTo.from, fromTo.to)

		println("Processed Person " + id)
	}
}
