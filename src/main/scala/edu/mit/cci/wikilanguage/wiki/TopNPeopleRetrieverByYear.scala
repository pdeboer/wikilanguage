package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO.Experiment
import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 16/12/13 at 14:16
 */
class TopNPeopleRetrieverByYear {
	def process(year:Int) {
		val people = DAO.getPopularPeopleByYearByIndegreeAndArticleSize(year, 5)

		people.foreach(p => {
			val aux = DAO.getPersonAux(p.personId)
			val ranking = aux.indegreeAlive.toDouble * aux.numChars.toDouble/4621 + aux.indegreeAlive.toDouble / aux.outdegreeAlive.toDouble
			DAO.storeTopIndegreePersonDouble(Experiment("Top5PeopleIndegreeAliveArticleSizeData", p.personId, year), ranking)
		})

		println("processed year "+year)
	}
}
