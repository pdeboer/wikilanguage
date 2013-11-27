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

	def containsNumber(str: String) = str.replaceAll("[^0-9]", "").length > 0

	def wikiUnify(name:String) = entityEscape(name.replaceAll(" ","_"))

	def entityEscape(in: String) =
		new URI("http", "//mit.edu/" + in, null).toASCIIString.substring("http://mit.edu/".length)

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
}
