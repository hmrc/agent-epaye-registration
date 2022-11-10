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

import org.mongodb.scala.model.Filters
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.Configuration
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.agentepayeregistration.models.{Address, AgentReference, RegistrationRequest}

import scala.collection.immutable.List

class AgentEpayeRegistrationRepositoryISpec extends BaseRepositoryISpec with BeforeAndAfterEach with BeforeAndAfterAll {
  private lazy val config = app.injector.instanceOf[Configuration]
  override lazy val repository = new AgentEpayeRegistrationRepository(mongoComponent, config)

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
    await(repository.ensureIndexes)
  }

  "AgentEpayeRegistrationRepository" should {
    "create a AgentReference record" in {
      await(repository.collection.find(Filters.equal("agentReference", "HX2000")).toFuture()) mustBe List.empty
      await(repository.create(regRequest))

      await(repository.collection.find(Filters.equal("agentReference", "HX2000")).head()) mustBe AgentReference("HX2000")
    }

    "create new records and generate a new unique Agent PAYE Reference code" in {
      await(repository.create(regRequest))
      await(repository.collection.find(Filters.equal("agentReference", "HX2000")).head()) mustBe AgentReference("HX2000")

      await(repository.create(regRequest))
      await(repository.collection.find(Filters.equal("agentReference", "HX2001")).head()) mustBe AgentReference("HX2001")

      await(repository.create(regRequest))
      await(repository.collection.find(Filters.equal("agentReference", "HX2002")).head()) mustBe AgentReference("HX2002")

    }
  }
}
