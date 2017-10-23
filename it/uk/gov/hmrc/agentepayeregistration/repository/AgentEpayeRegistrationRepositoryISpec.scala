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

package uk.gov.hmrc.agentepayeregistration.repository

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import uk.gov.hmrc.agentepayeregistration.models.{Address, AgentReference, RegistrationRequest}
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global

class AgentEpayeRegistrationRepositoryISpec extends BaseRepositoryISpec with MongoSpecSupport with BeforeAndAfterEach with BeforeAndAfterAll {
  private lazy val repo = app.injector.instanceOf[AgentEpayeRegistrationRepository]

  val postcode = "AB11 AA11"
  val addressLine1 = "Address Line 1"
  val addressLine2 = "Address Line 2"
  val addressLine3 = Some("Address Line 3")
  val addressLine4 = Some("Address Line 4")
  val regAddress = Address(addressLine1, addressLine2, addressLine3, addressLine4, postcode)

  val agentName = "Agent Name"
  val contactName = "Contact Name"
  val telephoneNumber = Some("0123456789")
  val faxNumber = Some("0123456780")
  val emailAddress = Some("a@b.com")
  val regDetails = RegistrationRequest(agentName, contactName, telephoneNumber, faxNumber, emailAddress, regAddress)

  override def beforeEach() {
    super.beforeEach()
    await(repo.ensureIndexes)
  }

  private def consumeToList[Item](e: Enumerator[Item]): List[Item] = await(e.run(Iteratee.getChunks[Item]))

  "AgentEpayeRegistrationRepository" should {
    "create a RegistrationDetails record" in {
      await(repo.find("agentName" -> agentName)) shouldBe List.empty

      val beforeCreation = DateTime.now(DateTimeZone.UTC)
      val result = await(repo.create(regDetails))
      val afterCreation = DateTime.now(DateTimeZone.UTC)

      result shouldBe AgentReference("HX2000")

      val details = await(repo.find("agentName" -> agentName)).head

      details should have(
        'agentReference (AgentReference("HX2000")),
        'registration (regDetails)
      )

      details.createdDateTime.getMillis should be >= (beforeCreation.getMillis)
      details.createdDateTime.getMillis should be <= (afterCreation.getMillis)
    }

    "create new records and generate a new unique Agent PAYE Reference code" in {
      await(repo.create(regDetails))
      await(repo.create(regDetails))
      await(repo.create(regDetails))

      val results = await(repo.find("agentName" -> agentName))

      results.size shouldBe 3

      results.head.agentReference shouldBe AgentReference("HX2000")
      results.drop(1).head.agentReference shouldBe AgentReference("HX2001")
      results.last.agentReference shouldBe AgentReference("HX2002")
    }

    "find registrations between a positive datetime range" in {
      val isoDateFmt = ISODateTimeFormat.date()

      val day1Start = DateTime.parse("2000-01-01", isoDateFmt)
      val day1End = day1Start.plusDays(1).minusMillis(1)
      val day2Start = DateTime.parse("2000-01-02", isoDateFmt)
      val day2End = day2Start.plusDays(1).minusMillis(1)
      val day3Start = DateTime.parse("2000-01-03", isoDateFmt)
      val day3End = day3Start.plusDays(1).minusMillis(1)

      await(repo.create(regDetails, day1Start))
      await(repo.create(regDetails, day2Start))
      await(repo.create(regDetails, day2Start))

      consumeToList(repo.enumerateRegistrations(day1Start, day1End)).size shouldBe 1
      consumeToList(repo.enumerateRegistrations(day3Start, day3End)).size shouldBe 0
      consumeToList(repo.enumerateRegistrations(day1Start, day3End)).size shouldBe 3
      consumeToList(repo.enumerateRegistrations(day2Start, day2End)).size shouldBe 2
    }

    "return found registrations in ascending time order" in {
      val isoDateFmt = ISODateTimeFormat.date()

      val day1 = DateTime.parse("2000-01-01", isoDateFmt)
      val day2 = DateTime.parse("2000-01-02", isoDateFmt)
      val day3 = DateTime.parse("2000-01-03", isoDateFmt)

      val regRefOnDay1 = await(repo.create(regDetails, day1))
      val regRefOnDay3 = await(repo.create(regDetails, day3))
      val regRefOnDay2 = await(repo.create(regDetails, day2))

      val endOfDay3 = day3.plusHours(24).minusMillis(1)
      val registrations = consumeToList(repo.enumerateRegistrations(day1, endOfDay3))
      registrations.map(_.agentReference) shouldBe Seq(regRefOnDay1, regRefOnDay2, regRefOnDay3)
    }

    "fail to find registration if the to date is before from date" in {
      val isoDateFmt = ISODateTimeFormat.date()
      val dateFrom = DateTime.parse("2000-01-01", isoDateFmt)
      val dateTo = dateFrom.minusDays(1)

      an[IllegalArgumentException] should be thrownBy {
        repo.enumerateRegistrations(dateFrom, dateTo)
      }
    }

    "count registrations in date range" in {
      val isoDateFmt = ISODateTimeFormat.date()

      val day1 = DateTime.parse("2000-01-01", isoDateFmt)
      val day2 = DateTime.parse("2000-01-02", isoDateFmt)
      val day3 = DateTime.parse("2000-01-03", isoDateFmt)

      await(repo.create(regDetails, day1))
      await(repo.create(regDetails, day2))
      await(repo.create(regDetails, day3))

      val endOfDay2 = day2.plusHours(24).minusMillis(1)
      val endOfDay3 = day3.plusHours(24).minusMillis(1)

      await(repo.countRecords(day1.withTimeAtStartOfDay(), endOfDay3)) shouldBe 3
      await(repo.countRecords(day1.withTimeAtStartOfDay(), endOfDay2)) shouldBe 2
      await(repo.countRecords(day2.withTimeAtStartOfDay(), endOfDay2)) shouldBe 1
    }
  }
}
