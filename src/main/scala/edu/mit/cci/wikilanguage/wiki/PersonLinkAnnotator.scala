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
			//function that returns the smaller of the given dates. if mul=-1, it returns the greater one
			val smallerDate = (d1:Date, d2:Date, mul:Int) => if(d1.compareTo(d2) * mul < 0) d1 else d2

			//smallest birth-year
			val minBirth:Date = targetDates.foldLeft[Date](null)( (r,c) =>
				if(c.from == null) r else if(r==null) c.from else smallerDate(r,c.from,1)  )

			//greatest death-year
			val maxDeath:Date = targetDates.foldLeft[Date](null)( (r,c) =>
				if(c.to == null) r else if(r==null) c.to else smallerDate(r,c.to,-1)  )

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
