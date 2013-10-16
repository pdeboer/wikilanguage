package edu.mit.cci.wikilanguage.model.active

import org.apache.commons.httpclient.{HttpMethod, HttpClient}
import org.apache.commons.httpclient.methods.GetMethod
import java.net.URLEncoder
import scala.xml.Source
import java.util.zip.GZIPInputStream
import java.io.{BufferedReader, InputStream, InputStreamReader}

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

 if too many members, use cmstart to retrieve next page
 */
class WikiCategory(val category: String, cmStart: String = null, val lang:String = "en", var id:Int = -1) {
  require(category != null)

  private var _contents: Array[String] = null

  //TODO clean entities
  def categoryClean = category.replaceAll(" ", "%20")

  def contents: Array[String] = {
    if (_contents == null) {
      val client = new HttpClient()
      val method = new GetMethod("http://"+lang+".wikipedia.org/w/api.php?action=query" +
        "&list=categorymembers&cmtitle=" + categoryClean + "&cmsort=timestamp&format=xml" +
        "&cmdir=desc&cmlimit=500" + (if (cmStart != null) "&cmstart=" + cmStart))
      // method.addRequestHeader("Accept-Charset", "utf-8")
      client.executeMethod(method)

      val data = method.getResponseBodyAsString()
      method.releaseConnection()

      //parse xml
      val xml = scala.xml.XML.loadString(data)
      val members = (xml \\ "cm").map(m => (m \ "@title").text)

      _contents = Array() ++ members

      //get other pages
      if ((xml \\ "query-continue").length == 1) {
        val newStart = xml \\ "query-continue" \  "categorymembers" \ "@cmstart"
        _contents ++= new WikiCategory(category, newStart.text).contents
      }
    }
    _contents
  }
}
