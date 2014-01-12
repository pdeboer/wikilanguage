package edu.mit.cci.wikilanguage.io

import java.io.{FileWriter, BufferedWriter}
import scala.collection.convert.WrapAsScala
import java.util.Collections

/**
 * @author pdeboer
 *         First created on 12/01/14 at 20:35
 */
class WekaExporter(val data: List[WekaLine]) {
	lazy val classNames = {
		val names = Collections.synchronizedSet[String](new java.util.HashSet[String])
		data.par.foreach(l => names.add(l.className))
		WrapAsScala.asScalaSet(names)
	}
	lazy val types = {
		val names = Collections.synchronizedSet[String](new java.util.HashSet[String])
		data.par.foreach(l => l.data.foreach(r => names.add(r._1)))
		WrapAsScala.asScalaSet(names)
	}

	def export(filename: String) {
		val out = new BufferedWriter(new FileWriter(filename))
		out.write("@relation Wikilanguage\n\n")
		types.foreach(l => out.write("@attribute " + l + " numeric\n"))
		out.write("@attribute class {" + classNames.mkString(",") + "}\n\n")
		out.write("@data\n")
		data.foreach(w => out.write(w.export + "\n"))
		out.close()
	}
}


trait WekaLine {
	def data: Map[String, String]

	def className: String

	def export = data.map(_._2).mkString(",") + "," + className
}

case class PeopleAuxWeka(val personId:Int, val indegree: Int, val outdegree: Int, val articleLength: Int, val pageRank: Double, val className: String) extends WekaLine {

	def data = List("personId"->personId.toString, "indegree" -> indegree.toString,
		"outdegree"-> outdegree.toString, "articleLength" -> articleLength.toString, 		"pageRank" -> pageRank.toString
	).toMap
}