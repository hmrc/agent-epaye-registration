/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.EitherValues

class UrlBindersSpec extends AnyWordSpecLike with Matchers with EitherValues {
  "LocalDate binder" should {
    "bind perfectly valid dates" in {
      UrlBinders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("2010-01-01"))) mustBe
        Some(Right(LocalDate.parse("2010-01-01", ISODateTimeFormat.date())))
    }
    "fail to bind parameter if not in the format yyyy-MM-dd" in {
      val expectedFailure = Some(Left("'From' date must be in ISO format (yyyy-MM-dd)"))
      UrlBinders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("yyyy-MM-dd"))) mustBe expectedFailure
      UrlBinders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("01-01-2017"))) mustBe expectedFailure
      UrlBinders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("2017 01 01"))) mustBe expectedFailure
      UrlBinders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("2017-01-01 19:16:39+01:00"))) mustBe expectedFailure
      UrlBinders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq(""))) mustBe expectedFailure
    }
    "fail to bind  a string in the format yyyy-MM-dd but not a valid date" in {
      val feb29NotInLeapYear = "2017-02-29"
      UrlBinders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq(feb29NotInLeapYear))) mustBe
        Some(Left("'From' date must be in ISO format (yyyy-MM-dd)"))
      UrlBinders.localDateQueryBinder.bind("dateTo", Map("dateTo" -> Seq(feb29NotInLeapYear))) mustBe
        Some(Left("'To' date must be in ISO format (yyyy-MM-dd)"))
    }
    "unbind dates" in {
      val someDate = LocalDate.parse("2010-01-01", ISODateTimeFormat.date())
      UrlBinders.localDateQueryBinder.unbind("Key", someDate) mustBe
        "dateKey=2010-01-01"
    }
  }
}
