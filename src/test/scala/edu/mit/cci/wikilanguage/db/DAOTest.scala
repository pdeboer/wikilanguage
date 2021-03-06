package edu.mit.cci.wikilanguage.db


import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.active.{WikiArticle, WikiCategory}
import edu.mit.cci.wikilanguage.model.{Category, Person}


/**
 * User: pdeboer
 * Date: 10/14/13
 * Time: 8:24 PM
 */
@RunWith(classOf[JUnitSuiteRunner])
class DAOTest  extends Specification with JUnit /*with ScalaCheck*/ {

  "DAO" should {
    DAO.clean()
    val category = new Category("category", lang="de")()
    val person = new Person("person", lang="de")()

    "be able to add new category" in {

      val cId = DAO.insertCategory(category)

      val catDB = DAO.categoryByName(category.name)

      catDB.id mustEqual cId
      catDB.name mustEqual category.name
      catDB.lang mustEqual category.lang
      catDB.id mustNotEq category.id
    }

    "be able to add a new person" in {

      val pId = DAO.insertPerson(person)

      val pDB = DAO.personByName(person.name)

      pDB.id mustEqual pId
      pDB.name mustEqual person.name
      pDB.lang mustEqual person.lang
      pDB.id mustNotEq person.id
    }

    "be able to add categories for people" in {
      person.categories = person.categories :+ category

      DAO.insertPerson(person)

      val p2 = DAO.personByName(person.name, true)

      person.categories(0) mustEqual p2.categories(0)
    }
  }
}


object DAOTest {
  def main(args: Array[String]) {
    new DAOTest().main(args)
  }

}
