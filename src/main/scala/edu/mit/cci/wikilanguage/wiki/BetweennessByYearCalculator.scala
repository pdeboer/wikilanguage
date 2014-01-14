package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO.Experiment
import edu.mit.cci.wikilanguage.db.DAO
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality
import edu.uci.ics.jung.graph.DirectedSparseGraph
import java.util
import scala.collection.JavaConversions._

/**
 * @author pdeboer
 *         First created on 16/12/13 at 14:16
 */
class BetweennessByYearCalculator {
	def process(year: Int) {
		println("processing "+year)
		val connections = DAO.getConnectionsByYear(year)

		val graph = new DirectedSparseGraph[Int, Int]
		connections.foreach(c => {
			if (!graph.containsVertex(c.personFromId)) graph.addVertex(c.personFromId)
			if (!graph.containsVertex(c.personToId)) graph.addVertex(c.personToId)

			graph.addEdge(c.id, c.personFromId, c.personToId)
		})

		val ranker = new BetweennessCentrality[Int, Int](graph)
		ranker.evaluate()

		val interestingPeople = new util.HashSet[Int](1000)
		DAO.getPeopleWithGivenDeathYear(year).foreach(interestingPeople.add(_))

		for (r <-  ranker.getVertexRankScores.values()) {
			for(v <- r.entrySet()) {
				if(interestingPeople.contains(v.getKey())) {
					DAO.storeTopIndegreePersonDouble(Experiment("BetweennessPerson", v.getKey(), year), v.getValue.doubleValue())
				}
			}
		}

		println("processed year " + year)
	}
}
