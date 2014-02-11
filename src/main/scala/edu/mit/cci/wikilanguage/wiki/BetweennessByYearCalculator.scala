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
import java.util.Date

/**
 * @author pdeboer
 *         First created on 16/12/13 at 14:16
 */
class BetweennessByYearCalculator {
	val experimentName: String = "BetweennessPerson"

	def process(year: Int, remainingTries:Int=3) {
		if(remainingTries == 0) return
		try {
			println("processing " + year)

			DAO.removeExperimentsInYear(experimentName, year)


			val connections = DAO.getFilteredConnectionsByYear(year)
			val graphName: String = "pr" + year
			val numShards: Int = 2
			val sharder = Pagerank.createSharder(graphName, numShards)
			connections.foreach(c => sharder.addEdge(c.personFromId, c.personToId, "1"))
			sharder.process()
			println("sharded " + year)

			val engine = new GraphChiEngine[java.lang.Float, java.lang.Float](graphName, numShards)
			engine.setEdataConverter(new FloatConverter)
			engine.setVertexDataConverter(new FloatConverter)
			engine.setModifiesInedges(false)
			engine.run(new Pagerank(), 4)

			println("calced pagerank " + year)

			val interestingPeople = new util.HashSet[Int](1000)
			(if (2014 == year) DAO.getAlivePeople() else DAO.getPeopleWithGivenDeathYear(year)).foreach(interestingPeople.add(_))


			val trans = engine.getVertexIdTranslate
			val top = Toplist.topListFloat(graphName, engine.numVertices(), 2000000) //2mio is more than the amount of ppl we got
			val highest = top.first().getValue.toDouble
			println(year + " highest pagerank: " + highest + " num vertices: " + engine.numVertices() + " num edges: " + engine.numEdges())

			top.foreach(i => {
				val personId = trans.backward(i.getVertexId)
				val pageRank = i.getValue.toDouble
				if (interestingPeople.contains(personId)) {
					DAO.storeTopIndegreePersonDouble(Experiment(experimentName, personId, year), pageRank/highest)
				}
			})

			println("processed year " + year)
		}
		catch {
			case t:Throwable => {
				println("problem when processing "+year+" - "+t.getMessage)
				t.printStackTrace(System.err)
				process(year, remainingTries-1)
			}
		}
	}
}
