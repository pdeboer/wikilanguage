package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO
import java.util.{Calendar, Date}
import edu.mit.cci.wikilanguage.model.{Person, Category}
import edu.mit.cci.util.U
import java.text.SimpleDateFormat

/**
 * @author pdeboer
 *         First created on 03/12/13 at 17:26
 */
class PersonLinkAnnotator {
	def processPerson(id: Int) {

	}
}

class PersonLinkTimestampDeterminer(val person:Person) {
	def outlinks = DAO.getPersonOutlinks(person.id)

	def determine = {
		val targetDates = person.categories.map(c => {
			val date = dateFromCategory(c)
			if (date == null) null
			else if (c.name.contains("birth")) FromTo(date, null)
			else if (c.name.contains("death")) FromTo(null, date)
			else null
		}).filter(_ != null)

		if (targetDates.size > 0) {
			val minBirth = targetDates.minBy(_.from).from
			val maxDeath = targetDates.maxBy(_.to).to

			FromTo(minBirth, maxDeath)
		} else FromTo(null, null)
	}

	/**
	 * works only for people that currently have a category BIRTH or DEATH
	 * @param c
	 * @return
	 */
	def dateFromCategory(c: Category): Date = {
		try {
			if (U.containsNumber(c.name) && U.checkStringContains(c.name, Array("births", "deaths"))) {
				val numberOfCharacters = "births".length //luckily, birth and death have the same length

				val withoutBeginning = c.name.substring("Category:".length)
				val withoutEnding = withoutBeginning.substring(0, withoutBeginning.length - numberOfCharacters)

				val isBC = withoutEnding.contains("BC")

				val candidateYear =
					U.getNumbers(withoutEnding).toInt * (if (withoutEnding.contains("century")) 100 else 1)

				new SimpleDateFormat("yyyy G").parse(candidateYear + (if (isBC) " BC" else " AD"))
			} else null
		}
		catch {
			case e:Throwable => null
		}
	}

}

case class FromTo(from: Date, to: Date)
