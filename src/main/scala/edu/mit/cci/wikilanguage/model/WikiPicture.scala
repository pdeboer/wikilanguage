package edu.mit.cci.model.active

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod

/**
 * @author pdeboer
 *         First created on 8/13/13 at 4:41 PM
 */
class WikiPicture(val url: String) {
	require(url != null)
	require(mainCandidate != null)

	private var _bytes: Array[Byte] = null
	private var _contentType: String = null
	var mainCandidate: Boolean = false

	def bytes: Array[Byte] = {
		if (_bytes == null) {
			try {
				val http = new HttpClient()
				val method = new GetMethod(url)

				http.executeMethod(method)

				_bytes = method.getResponseBody
				_contentType = method.getResponseHeader("Content-Type").getValue

				method.releaseConnection()
			}
			catch {
				case i: Exception => null
			}
		}

		_bytes
	}

	def contentType: String = {
		bytes //make sure we downloaded the page

		_contentType
	}
}
