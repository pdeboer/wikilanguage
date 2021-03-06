package edu.mit.cci.util

import org.jsoup.select.Elements
import org.jsoup.nodes.Element
import org.apache.commons.httpclient.{HttpConnection, HostConfiguration, MultiThreadedHttpConnectionManager, HttpClient}
import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import java.net.URI

/**
 * User: pdeboer
 * Date: 8/6/13
 * Time: 7:47 PM
 */
object U {
	def foreachJSoupElement(lis: Elements, f: (Element => Unit)) = {
		for {i <- 0 to lis.size() - 1; li = lis.get(i)} f(li)
	}

	def convertJSoupToList(lis: Elements): List[Element] = {
		var ret: List[Element] = Nil
		foreachJSoupElement(lis, ret ::= _)
		ret
	}

	def containsNumber(str: String) = getNumbers(str).length > 0

	def getNumbers(str:String )= str.replaceAll("[^0-9]","")

	def wikiUnify(name:String) = entityEscape(name.replaceAll(" ","_"))

	def entityEscape(in: String) =
		new URI("http", "//mit.edu/" + in, null).toASCIIString.substring("http://mit.edu/".length)

	def checkStringContainsOne(str: String, contains: Array[String]): Boolean = {
		val lowerCaseString = str.toLowerCase()

		contains.exists(lowerCaseString.contains(_))
	}

	def stripNonBmpUTF(in:String) =  in.replaceAll("[^\\u0000-\\uFFFF]", "")

	def checkStringContainsAll(str: String, contains: Array[String], matchCase:Boolean=true): Boolean = {
		val cmp = if(matchCase) str else str.toLowerCase()

		contains.forall(cmp.contains(_))
	}

	private var _httpClient: HttpClient = null
	private val sourceEmail = "reobedp".reverse + "@mit.edu"

	def httpClient(): HttpClient = {
		if (_httpClient == null) {
			val httpClientManager: MultiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager()
			val params = new HttpConnectionManagerParams
			params.setMaxTotalConnections(50)
			class AcceptingHostConfiguration extends HostConfiguration {
				override def hostEquals(connection: HttpConnection): Boolean = true
			}

			val hostConfig = new AcceptingHostConfiguration
			params.setMaxConnectionsPerHost(hostConfig, 50)
			params.setConnectionTimeout(1000*10) //10 seconds
			params.setSoTimeout(1000*10)
			httpClientManager.setParams(params)
			_httpClient = new HttpClient(httpClientManager)
			_httpClient.getParams().setParameter("http.useragent", "WikiLanguage 0.1 (Comparing people graph of different languages) "+sourceEmail)
		}
		_httpClient
	}

	def exceptionHasCase[A](t:Throwable):Boolean = {
		if(t.isInstanceOf[A]) true
		else if(t.getCause!=null) exceptionHasCase[A](t.getCause)
		else false
	}
}
