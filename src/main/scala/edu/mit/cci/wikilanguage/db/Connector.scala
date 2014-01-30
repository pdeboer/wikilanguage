package edu.mit.cci.db

import java.sql._
import scala.collection.mutable
import java.util.concurrent.Semaphore

/**
 * User: pdeboer
 * Date: 8/2/13
 * Time: 6:18 PM
 */
object Connector {
	private val connections = new mutable.Queue[Connection]()

	private val counter = new Semaphore(100)

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
		if (conn == null) {
			conn = if(counter.availablePermits() > 0) getNewConnection() else {
				Thread.sleep(1000)
				getConnection
			}
		}

		if (!conn.isValid(10)) {
			counter.release()
			conn = getNewConnection()
		}

		conn
	}

	def offer(c: Connection) {
		//add connection add end of queue
		connections.synchronized {
			connections += c
		}
	}

	private def getNewConnection() = {
		Class.forName("com.mysql.jdbc.Driver")
		counter.acquire()

		val connection: Connection = DriverManager.getConnection("jdbc:mysql://localhost/wikilanguage2?useUnicode=true&characterEncoding=UTF-8", "wikilanguage", "wikilanguage")

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


	def insertReturnID(query: String)(f: (PreparedStatement) => Unit): Long = {
		var insertId = -1L

		autoClose(getConnection) {
			conn => {
				if (conn == null) return -1L


				autoClose(conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
					stmt =>
						f(stmt)
						stmt.executeUpdate()

						val rs = stmt.getGeneratedKeys
						if(rs!= null && rs.next()) {
							insertId = rs.getInt(1)
							rs.close()
						}
				}
			}
		}

		insertId
	}

	def autoClose[T](closable: T)(f: T => Unit) {
		try {
			f(closable)
		} catch {
			case e: Exception => throw new RuntimeException(e)
		}
		finally {
			closable match {
				case c: Connection => offer(c)
				case r: ResultSet => r.close()
				case s: PreparedStatement => s.close()
			}
		}
	}
}
