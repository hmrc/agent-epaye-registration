/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.agentepayeregistration.controllers

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc.QueryStringBindable

import scala.util.Try

object UrlBinders {
  implicit def localDateQueryBinder = new QueryStringBindable[LocalDate] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] = {
      params.get(key).flatMap(_.headOption).map { dateTxt: String => (Try {
        Right(LocalDate.parse(dateTxt, ISODateTimeFormat.date()))
      } recover {
        case _: Exception => Left(s"'${key.replaceFirst("date", "")}' date must be in ISO format (yyyy-MM-dd)")
      }).get
      }
    }

    override def unbind(key: String, value: LocalDate): String = QueryStringBindable.bindableString.unbind(s"date$key", value.toString)
  }
}
