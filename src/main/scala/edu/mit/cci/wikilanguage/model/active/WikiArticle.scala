package edu.mit.cci.wikilanguage.model.active

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
import scala.xml.XML

/**
 * User: pdeboer
 * Date: 8/6/13
 * Time: 7:39 PM
 *
 * rich class that fetches data from wikipedia if needed. starts off sparse
 */
class WikiArticle(val name: String, val lang: String = "en", var id:Int = -1) {
  private var _content: String = ""
  private var _parsed: Element = null
  private var _categories: Array[WikiCategory] = null

  def text: String = {
    if (_content == "") {
      val client = new HttpClient()
      //val method = new GetMethod("http://en.wikipedia.org/w/index.php?title=" + URLEncoder.encode(name) + "&action=raw")
      val method = new GetMethod("http://" + lang + ".wikipedia.org/wiki/" + URLEncoder.encode(name))
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
      val client = new HttpClient()
      val method = new GetMethod("http://" + lang + ".wikipedia.org/w/api.php?" +
        "format=xml&action=query&prop=categories&titles=" + name + "&cllimit=500")
      method.addRequestHeader("Accept-Charset", "utf-8")
      client.executeMethod(method)
      val data = method.getResponseBodyAsString
      method.releaseConnection()

      val xml = XML.loadString(data)
      val cat = (xml \\ "cl" \\ "@title").map(c => new WikiCategory(c.text, lang = lang))
      _categories = Array() ++ cat
    }
    _categories
  }

  def titleInOtherLanguages: List[String] = {
    //TODO: code me
    Nil
  }
}
