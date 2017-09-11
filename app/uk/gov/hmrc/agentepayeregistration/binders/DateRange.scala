/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentepayeregistration.binders

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc.QueryStringBindable

import scala.util.Try

case class DateRange(from: LocalDate, to: LocalDate)

object DateRange {
  implicit def localDateQueryBinder() = new QueryStringBindable[LocalDate] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] = {
      params.get(s"date$key").flatMap(_.headOption).map { dateTxt: String => (Try {
        Right(LocalDate.parse(dateTxt, ISODateTimeFormat.date()))
      } recover {
        case _: Exception => Left(s"'$key' date must be in ISO format (yyyy-MM-dd)")
      }).get
      }
    }

    override def unbind(key: String, value: LocalDate): String = QueryStringBindable.bindableString.unbind(s"date$key", value.toString)
  }

  implicit def dateRangeQueryBinder(localDateBinder: QueryStringBindable[LocalDate]) = new QueryStringBindable[DateRange] {
    override def bind(key: String, params: Map[String, Seq[String]]) = {
      for {
        from <- localDateBinder.bind("From", params)
        to <- localDateBinder.bind("To", params)
      } yield {
        (from, to) match {
          case (Right(from), Right(to)) => Right(DateRange(from, to))
          case _ => Left("Unable to bind DateRange")
        }
      }
    }

    override def unbind(key: String, dateRange: DateRange): String = {
      val fromParam = localDateBinder.unbind("From", dateRange.from)
      val toParam = localDateBinder.unbind("To", dateRange.to)
      s"$fromParam&$toParam"
    }
  }
}