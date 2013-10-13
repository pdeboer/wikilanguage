package edu.mit.cci.db

import java.sql.{ResultSet, PreparedStatement, DriverManager, Connection}
import com.jolbox.bonecp.{BoneCP, BoneCPConfig}
import scala.collection.mutable

/**
 * User: pdeboer
 * Date: 8/2/13
 * Time: 6:18 PM
 */
object Connector {
	val connections = new mutable.Queue[Connection]()

	def getConnection: Connection = {
		//pool.getConnection


		var conn: Connection = null
		connections.synchronized {
			val firstElement = connections.headOption
			conn = firstElement.getOrElse(getNewConnection())
			if (!firstElement.isEmpty) connections.dequeue() //remove first
		}

		if (!conn.isValid(10))
			conn = getConnection

		conn
	}

	private def getNewConnection() = {
		Class.forName("com.mysql.jdbc.Driver")
		val connection: Connection = DriverManager.getConnection("jdbc:mysql://localhost/wikilanguage?useUnicode=true&characterEncoding=UTF-8", "root", "")

		connections.synchronized {
			connections += connection
		}

		println("established new connection")

		connection

	}

	def autoClose[T](closable: T)(f: T => Unit) {
		try {
			f(closable)
		}
		catch {
			case e: Exception => e.printStackTrace()
		}
		finally {
			closable match {
				case c: Connection => connections += c
				case r: ResultSet => r.close()
				case s: PreparedStatement => s.close()
			}
		}
	}
}
