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
import uk.gov.hmrc.agentepayeregistration.models.{Address, AgentReference, RegistrationDetails, RegistrationRequest}
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
  val regRequest = RegistrationRequest(agentName, contactName, telephoneNumber, faxNumber, emailAddress, regAddress)


  override def beforeEach() {
    super.beforeEach()
    await(repo.ensureIndexes)
  }

  private def consumeToList[Item](e: Enumerator[Item]): List[Item] = await(e.run(Iteratee.getChunks[Item]))

  "AgentEpayeRegistrationRepository" should {
    "create a AgentReference record" in {
      await(repo.find("agentReference" -> "HX2000")) shouldBe List.empty
      await(repo.create(regRequest))

      await(repo.find("agentReference" -> "HX2000")).head shouldBe AgentReference("HX2000")
    }

    "create new records and generate a new unique Agent PAYE Reference code" in {
      await(repo.create(regRequest))
      await(repo.find("agentReference" -> "HX2000")).head shouldBe AgentReference("HX2000")

      await(repo.create(regRequest))
      await(repo.find("agentReference" -> "HX2001")).head shouldBe AgentReference("HX2001")

      await(repo.create(regRequest))
      await(repo.find("agentReference" -> "HX2002")).head shouldBe AgentReference("HX2002")

    }

  }
}
