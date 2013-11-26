package edu.mit.cci.wikilanguage.model.active

import org.apache.commons.httpclient.methods.GetMethod
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import edu.mit.cci.util.U
import scala.xml.XML

/**
 * User: pdeboer
 * Date: 8/6/13
 * Time: 7:39 PM
 *
 * rich class that fetches data from wikipedia if needed. starts off sparse
 */
class WikiArticle(val name: String, val lang: String = "en") {
	private var _content: String = ""
	private var _parsed: Element = null
	private var _categories: Array[WikiCategory] = null

	def text: String = {
		if (_content == "") {
			val client = U.httpClient()
			//val method = new GetMethod("http://en.wikipedia.org/w/index.php?title=" + URLEncoder.encode(name) + "&action=raw")
			val method = new GetMethod("http://" + lang + ".wikipedia.org/wiki/" + nameCleaned)
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

	def categories = {
		if (_categories == null) {
			val client = U.httpClient()
			try {
				val method = new GetMethod("http://" + lang + ".wikipedia.org/w/api.php?" +
				  "format=xml&action=query&prop=categories&titles=" + nameCleaned + "&cllimit=500")
				method.addRequestHeader("Accept-Charset", "utf-8")
				client.executeMethod(method)
				val data = method.getResponseBodyAsString
				method.releaseConnection()

				val xml = XML.loadString(data)
				val cat = (xml \\ "cl" \\ "@title").map(c => new WikiCategory(c.text, lang = lang))
				_categories = Array() ++ cat
			}
			catch {
				case e: Exception => {
					println("Couldn't get contents of category " + name)
					e.printStackTrace(System.err)
					_categories = Array()
				}
			}
		}
		_categories
	}

	/**
	 * find names of all articles this article is referencing
	 * @return
	 */
	def outlinks(): List[String] = {
		try {
			//fetch all outgoing links of article and return their names
			val linkElements = U.convertJSoupToList(parsed.select("a"))
			val cleanedList = linkElements.filter(a => loc(a) != null && loc(a).length > 0).map(loc(_))
			val links = cleanedList.map(l => {
				val u = urlStart.find(l.startsWith(_))

				if(!u.isEmpty) l.substring(u.get.length)
				else null
			})

			links.filterNot(_==null)
		}
		catch {
			case e: Exception => {
				println("couldnt parse " + name)
				List.empty[String]
			}
		}

	}

	private def urlStart = Array("http://" + lang + ".wikipedia.org/wiki/", "/wiki/", "/w/")

	def likelyPersonOutlinks():List[String] = {
		outlinks.filter(u => !u.contains(":") && !u.startsWith("index.php"))
	}

	private def loc(e:Element):String = e.attr("href")


	def nameCleaned: String = U.entityEscape(name)

	def titleInOtherLanguages: List[String] = {
		//TODO: code me
		Nil
	}
}
