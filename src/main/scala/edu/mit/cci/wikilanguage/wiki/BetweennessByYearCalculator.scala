package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO.Experiment
import edu.mit.cci.wikilanguage.db.DAO
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality
import edu.uci.ics.jung.graph.DirectedSparseGraph
import java.util
import scala.collection.JavaConversions._
import edu.cmu.graphchi.preprocessing.{EdgeProcessor, FastSharder, VertexProcessor}
import edu.cmu.graphchi.datablocks.FloatConverter
import edu.cmu.graphchi.apps.Pagerank
import edu.cmu.graphchi.engine.GraphChiEngine
import edu.cmu.graphchi.util.Toplist

/**
 * @author pdeboer
 *         First created on 16/12/13 at 14:16
 */
class BetweennessByYearCalculator {
	def process(year: Int) {
		println("processing "+year)

		val connections = DAO.getConnectionsByYear(year)

		val graphName: String = "pr" + year
		val sharder = Pagerank.createSharder(graphName, 5)
		connections.foreach(c => sharder.addEdge(c.personFromId, c.personToId, "1"))
		sharder.process()
		println("sharded "+year)

		val engine = new GraphChiEngine[java.lang.Float, java.lang.Float](graphName, 5)
		engine.setEdataConverter(new FloatConverter)
		engine.setVertexDataConverter(new FloatConverter)
		engine.setModifiesInedges(false)
		engine.run(new Pagerank(), 4)

		println("calced pagerank "+year)

		val interestingPeople = new util.HashSet[Int](1000)
		DAO.getPeopleWithGivenDeathYear(year).foreach(interestingPeople.add(_))


		val trans = engine.getVertexIdTranslate
		val top = Toplist.topListFloat(graphName, engine.numVertices(), 2000000) //2mio is more than the amount of ppl we got
		top.foreach(i=>{
			val personId = trans.backward(i.getVertexId)
			val pageRank = i.getValue

			if(interestingPeople.contains(personId)) {
				DAO.storeTopIndegreePersonDouble(Experiment("BetweennessPerson", personId, year), pageRank)
			}
		})

		println("processed year " + year)
	}
}
