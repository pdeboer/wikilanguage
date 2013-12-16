package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO.Experiment
import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 16/12/13 at 14:16
 */
class TopNPeopleRetrieverByYear {
	def process(year:Int) {
		val people = DAO.getPopularPeopleByYearByIndegree(year, 20)

		people.foreach(p => {
			DAO.storeTopIndegreePerson(Experiment("Top20PeopleIndegree", p.personId, year), p.degree)
		})

		println("processed year "+year)
	}
}
