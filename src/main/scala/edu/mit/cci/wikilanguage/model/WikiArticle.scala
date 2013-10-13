package edu.mit.cci.model.active

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.jsoup.Jsoup
import org.jsoup.nodes.{Element, Document}
import info.bliki.wiki.model.WikiModel
import java.net.URLEncoder
import org.mozilla.universalchardet.UniversalDetector
import scala.io.Source
import scala.None
import edu.mit.cci.util.U

/**
 * User: pdeboer
 * Date: 8/6/13
 * Time: 7:39 PM
 *
 * rich class that fetches data from wikipedia if needed. starts off sparse
 */
class WikiArticle(val name: String, val lang:String ="en") {
	private var _content: String = ""
	private var _parsed: Element = null

	def text: String = {
		if (_content == "") {
			val client = new HttpClient()
			//val method = new GetMethod("http://en.wikipedia.org/w/index.php?title=" + URLEncoder.encode(name) + "&action=raw")
			val method = new GetMethod("http://"+lang+".wikipedia.org/wiki/" + URLEncoder.encode(name))
			method.addRequestHeader("Accept-Charset", "utf-8")
			client.executeMethod(method)

			_content = method.getResponseBodyAsString

			method.releaseConnection()
		}
		_content
	}

	def parsed: Element = {
		if (_parsed == null && text != null) {
			val pcontent = Jsoup.parse(text).select("div#mw-content-text")
			if (pcontent != null) {
				_parsed = pcontent.first()
			}
		}
		_parsed
	}

	/**
	 * yields intro text just below title on html article
	 * @return
	 */
	def pitch: String = {
		if (parsed == null) return ""

		val ps = parsed.select("p")
		if (ps.size() == 0) return parsed.text()
		else return ps.get(0).text()
	}

	def titleInOtherLanguages: List[String] = {
		//TODO: code me
		Nil
	}
}
