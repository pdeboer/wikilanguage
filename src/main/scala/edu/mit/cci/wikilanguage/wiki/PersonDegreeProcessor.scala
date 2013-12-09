package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 09/12/13 at 11:14
 */
class PersonDegreeProcessor {
	def process(person:Int) {
		val deg = DAO.getPersonDegrees(person)
		if(deg != null) DAO.storePersonDegrees(person, deg)
		println("processed degree of person "+person)
	}
}
