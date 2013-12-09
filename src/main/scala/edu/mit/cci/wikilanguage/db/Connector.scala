package edu.mit.cci.db

import java.sql._
import scala.collection.mutable

/**
 * User: pdeboer
 * Date: 8/2/13
 * Time: 6:18 PM
 */
object Connector {
	private val connections = new mutable.Queue[Connection]()

	def getConnection: Connection = {
		var conn: Connection = null
		connections.synchronized {
			// had to use ugly try-catch because scala support for headOption
			// couldn't cope with a highly parallel environment
			conn = try {
				connections.dequeue
			}
			catch {
				case e: Exception => null
			}
		}
		if (conn == null) conn = getNewConnection()

		if (!conn.isValid(10))
			conn = getConnection

		conn
	}

	private def getNewConnection() = {
		Class.forName("com.mysql.jdbc.Driver")
		val connection: Connection = DriverManager.getConnection("jdbc:mysql://localhost/wikilanguage2?useUnicode=true&characterEncoding=UTF-8", "wikilanguage", "wikilanguage")

		connections.synchronized {
			connections += connection
		}

		println("established new connection")

		connection

	}

	def autoCloseStmt(query: String)(f: (PreparedStatement) => Unit): Boolean = {
		autoClose(getConnection) {
			conn => {
				if (conn == null) return false

				autoClose(conn.prepareStatement(query)) {
					stmt =>
						f(stmt)
						stmt.execute()
				}
			}
		}
		return true
	}


	def autoClose[T](closable: T)(f: T => Unit) {
		try {
			f(closable)
		} catch {
			case e: Exception => throw new RuntimeException(e)
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
