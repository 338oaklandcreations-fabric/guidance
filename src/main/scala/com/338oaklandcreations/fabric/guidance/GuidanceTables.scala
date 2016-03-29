/*

    Copyright (C) 2016 Mauricio Bustos (m@bustos.org) & 338.oakland creations

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package com._338oaklandcreations.fabric.guidance

import akka.util.ByteString
import org.joda.time._
import java.sql.Timestamp
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat, DateTimeFormatter}

import scala.slick.driver.MySQLDriver.simple._
import spray.json._

import scala.util.Properties._

object GuidanceTables {

  // Base case classes
  case class BaseRecord(comment: String, timestamp: DateTime)

  val baseTable = TableQuery[BaseTable]

  val formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss")

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
  implicit def dateTime =
    MappedColumnType.base[DateTime, Timestamp](
       dt => new Timestamp(dt.getMillis),
       ts => new DateTime(ts.getTime)
    )
}

object GuidanceJsonProtocol extends DefaultJsonProtocol {

  import GuidanceTables._

  implicit object DateJsonFormat extends RootJsonFormat[DateTime] {
    private val parserISO: DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis()
    private val parserMillisISO: DateTimeFormatter = ISODateTimeFormat.dateTime()
    override def write(obj: DateTime) = JsString(parserISO.print(obj))
    override def read(json: JsValue) : DateTime = json match {
      case JsString(s) =>
        try {
          parserISO.parseDateTime(s)
        } catch {
          case _: Throwable => parserMillisISO.parseDateTime(s)
        }
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  // Base case classes
  implicit val baseRecord = jsonFormat4(BaseRecord)
}

class BaseTable(tag: Tag) extends Table[GuidanceTables.BaseRecord](tag, "baseRecord") {
  import GuidanceTables.dateTime

  def comment = column[String]("comment")
  def resultTimeStamp = column[DateTime]("resultTimeStamp")

  def * = (comment, resultTimeStamp) <> (GuidanceTables.BaseRecord.tupled, GuidanceTables.BaseRecord.unapply)
}