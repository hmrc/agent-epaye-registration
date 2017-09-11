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
import org.scalatest.{EitherValues, Matchers, WordSpecLike}

class BindersSpec extends WordSpecLike with Matchers with EitherValues {
  "LocalDate binder" should {
    "bind perfectly valid dates" in {
      Binders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("2010-01-01"))) shouldBe
        Some(Right(LocalDate.parse("2010-01-01", ISODateTimeFormat.date())))
    }
    "fail to bind parameter if not in the format yyyy-MM-dd" in {
      val expectedFailure = Some(Left("'From' date must be in ISO format (yyyy-MM-dd)"))
      Binders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("yyyy-MM-dd"))) shouldBe expectedFailure
      Binders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("01-01-2017"))) shouldBe expectedFailure
      Binders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("2017 01 01"))) shouldBe expectedFailure
      Binders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq("2017-01-01 19:16:39+01:00"))) shouldBe expectedFailure
      Binders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq(""))) shouldBe expectedFailure
    }
    "fail to bind  a string in the format yyyy-MM-dd but not a valid date" in {
      val feb29NotInLeapYear = "2017-02-29"
      Binders.localDateQueryBinder.bind("dateFrom", Map("dateFrom" -> Seq(feb29NotInLeapYear))) shouldBe
        Some(Left("'From' date must be in ISO format (yyyy-MM-dd)"))
      Binders.localDateQueryBinder.bind("dateTo", Map("dateTo" -> Seq(feb29NotInLeapYear))) shouldBe
        Some(Left("'To' date must be in ISO format (yyyy-MM-dd)"))
    }
    "unbind dates" in {
      val someDate = LocalDate.parse("2010-01-01", ISODateTimeFormat.date())
      Binders.localDateQueryBinder.unbind("Key", someDate) shouldBe
        "dateKey=2010-01-01"
    }
  }
}
