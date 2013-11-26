package edu.mit.cci.wikilanguage.model

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.active.WikiArticle
import edu.mit.cci.wiki.ArticleCache

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class WikiArticleTest extends Specification with JUnit /*with ScalaCheck*/ {
	"WikiArticle" should {
		"be able to retrieve contents of an article" in {
			val w = new WikiArticle("Google")

			w.categories.length mustNotEq 0
		}

		"find outlinks for a basic use case" in {
			val article = ArticleCache.get("Thomas W. Malone")
			val outlinks = article.likelyPersonOutlinks()
			outlinks.size > 10 must beTrue
		}
	}
}

object WikiArticleTest {
	def main(args: Array[String]) {
		new WikiCategoryTest().main(args)
	}
}
