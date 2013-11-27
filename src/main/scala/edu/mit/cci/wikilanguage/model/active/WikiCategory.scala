package edu.mit.cci.wikilanguage.model.active

import org.apache.commons.httpclient.{HttpMethod, HttpClient}
import org.apache.commons.httpclient.methods.GetMethod
import java.net.URLEncoder
import scala.xml.{Elem, Source}
import java.util.zip.GZIPInputStream
import java.io.{BufferedReader, InputStream, InputStreamReader}
import edu.mit.cci.util.U
import scala.util.Random

/**
 * User: pdeboer
 * Date: 10/12/13
 * Time: 10:08 PM
 *
 * sample result: (http://en.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:19th-century_German_painters&cmsort=timestamp&cmdir=desc&cmlimit=500)
 *
 * <?xml version="1.0"?>
<api>
  <query-continue>
    <categorymembers cmstart="2013-07-07T14:59:14Z" />
  </query-continue>
  <query>
    <categorymembers>
      <cm pageid="40601636" ns="0" title="Fedor Flinzer" />
      <cm pageid="40499851" ns="0" title="Gotthardt Kuehl" />
      <cm pageid="40492280" ns="0" title="Max Fleischer (painter)" />

 if members exceed fetch-limit, use cmstart to retrieve next page
 */
class WikiCategory(val category: String, cmStart: String = null, val lang: String = "en") {
	require(category != null)

	private var _contents: Array[String] = null
	private val DEFAULT_TRIES_FETCHER = 5

	def categoryClean = U.entityEscape(category)

	def contents: Array[String] = {
		if (_contents == null) {
			//parse xml
			val xmlAndMembers = fetchContents()
			if (xmlAndMembers == null) _contents = Array.empty[String]
			else {
				_contents = xmlAndMembers.contents

				//get other pages
				_contents ++= fetchRemainingPages(xmlAndMembers.xml)

				_contents.filter(_ != null) //remove null values
			}
		}
		_contents
	}

	def fetchRemainingPages(xml: Elem): Array[String] = {
		//get other pages
		try {
			if ((xml \\ "query-continue").length == 1) {
				val newStart = xml \\ "query-continue" \ "categorymembers" \ "@cmstart"
				return new WikiCategory(category, newStart.text).contents
			}
		}
		catch {
			case e: Exception => {
				System.out.println("problems fetching rest of " + category + " " + e + " " + e.getMessage)
			}
		}
		Array.empty[String] //fallback
	}

	protected case class XMLAndContents(xml: Elem, contents: Array[String])

	def fetchContents(tries: Int = DEFAULT_TRIES_FETCHER): XMLAndContents = {
		if (tries <= 0) return null

		if (tries < DEFAULT_TRIES_FETCHER) println(tries + " try for " + category)

		try {
			val client = U.httpClient()
			val method = new GetMethod("http://" + lang + ".wikipedia.org/w/api.php?action=query" +
			  "&list=categorymembers&cmtitle=" + categoryClean + "&cmsort=timestamp&format=xml" +
			  "&cmdir=desc&cmlimit=500" + (if (cmStart != null) "&cmstart=" + cmStart))
			// method.addRequestHeader("Accept-Charset", "utf-8")

			//delay and get data
			//Thread.sleep((1000 * 10 * new java.util.Random().nextFloat()).asInstanceOf[Long])
			client.executeMethod(method)

			val data = method.getResponseBodyAsString()

			method.releaseConnection()

			val xml = scala.xml.XML.loadString(data)
			val members = (xml \\ "cm").map(m => (m \ "@title").text)
			if (members.size == 0) return fetchContents(tries - 1)

			return XMLAndContents(xml, Array.empty[String] ++ members)
		}
		catch {
			case e: Throwable => return fetchContents(tries - 1)
		}
	}
}
