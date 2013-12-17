package edu.mit.cci.wikilanguage.io

import edu.mit.cci.wikilanguage.db.DAO
import java.io.{BufferedWriter, FileWriter}

/**
 * @author pdeboer
 *         First created on 16/12/13 at 11:52
 */
class FileExporter {
	var data = List.empty[GraphVizLine]

	def init(ids: List[Int], printNames: Boolean = false) {
		ids.par.foreach(from => {
			val fromPerson = DAO.personById(from)
			DAO.getPersonOutlinks(from).map(to => {
				val toPerson = DAO.personById(to.personToId)
				data ::= GraphVizLine(fromPerson.name, toPerson.name, 1)
			})
			if (printNames) println("processed " + from)
		})
	}

	def exportDot(filename: String = "out.dot") {
		val out = new BufferedWriter(new FileWriter(filename))
		out.write("digraph PeopleConnections {\n")
		data.foreach(l => out.write(l.dotLine + "\n"))
		out.write("}")
		out.close()
	}

	def exportEdgeList(filename: String = "out.csv") {
		val out = new BufferedWriter(new FileWriter(filename))
		data.foreach(l => out.write(l.edgeListLine + "\n"))
		out.close()
	}
}

case class GraphVizLine(from: String, to: String, weight: Int) {
	def dotLine = "\"" + from + "\" -> \"" + to + "\" [weight=" + weight + "]"

	def edgeListLine = from + " " + to + " " + weight
}
