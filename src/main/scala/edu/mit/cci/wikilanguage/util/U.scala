package edu.mit.cci.util

import org.jsoup.select.Elements
import org.jsoup.nodes.Element
import org.apache.commons.httpclient.{HostConfiguration, MultiThreadedHttpConnectionManager, HttpClient}
import org.apache.commons.httpclient.params.HttpConnectionManagerParams

/**
 * User: pdeboer
 * Date: 8/6/13
 * Time: 7:47 PM
 */
object U {
	def foreachJSoupElement(lis:Elements,f:(Element => Unit)) = {
		for {i <- 0 to lis.size()-1; li = lis.get(i)} f(li)
	}

	def convertJSoupToList(lis:Elements):List[Element] = {
		var ret:List[Element] = Nil
		foreachJSoupElement(lis, ret ::= _)
		ret
	}

  private var _httpClient:HttpClient = null
  def httpClient():HttpClient = {
    if(_httpClient == null) {
      val httpClientManager:MultiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager()
      val params = new HttpConnectionManagerParams
      params.setMaxTotalConnections(50)
      val hostConfig = new HostConfiguration()
      hostConfig.setHost("wikipedia.org")
      params.setMaxConnectionsPerHost(hostConfig, 50)
      httpClientManager.setParams(params)
      _httpClient = new HttpClient(httpClientManager)
    }
    _httpClient
  }
}
