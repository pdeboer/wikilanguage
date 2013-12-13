package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 09/12/13 at 11:14
 */
class PersonDegreeProcessor {
	def getDegrees(person:Int) = {
		DAO.getPersonDegrees(person)
	}
}
