package edu.mit.cci.wikilanguage.io

import edu.mit.cci.wikilanguage.db.DAO
import java.io.{BufferedWriter, FileWriter}

/**
 * @author pdeboer
 *         First created on 16/12/13 at 11:52
 */
class GraphVizExporter {
	var data: List[GraphVizLine] = Nil

	def init(ids: List[Int], printNames:Boolean = false) {
		data = ids.par.map(from => {
			val fromPerson = DAO.personById(from)
			DAO.getPersonOutlinks(from).map(to => {
				val toPerson = DAO.personById(to.personToId)
				GraphVizLine(fromPerson.name, toPerson.name, 1)
			})
			if(printNames) println("processed "+from)
		}).flatten.filter(_ != null).toList
	}

	def exportDot(filename: String = "out.dot") {
		val out = new BufferedWriter(new FileWriter(filename))
		out.write("digraph PeopleConnections {\n")
		data.foreach(l => out.write(l.line + "\n"))
		out.write("}")
		out.close()
	}
}

case class GraphVizLine(from: String, to: String, weight: Int) {
	def line = "\"" + from + "\" -> \"" + to + "\" [weight=" + weight + "]"
}
