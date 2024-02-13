/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.agentepayeregistration.connectors

import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.agentepayeregistration.models._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class DesConnectorISpec extends BaseConnectorISpec {

  implicit val hc = HeaderCarrier()
  val connector = app.injector.instanceOf[DesConnector]

  val regRequest = RegistrationRequest("Alex P", "Alex P", Some("199999"), Some("11 111 0000"), Some("a@a.com"),
    Address("6 High Street", "Kidderminster", None, None, "TF3 4ER"))

  val invalidRegRequest = RegistrationRequest("'INVALID PAYLOAD'", "Alex P", Some("199999"), Some("11 111 0000"), Some("a@a.com"),
    Address("6 High Street", "Kidderminster", None, None, "TF3 4ER"))

  val createKnownFactsRequest = CreateKnownFactsRequest(regRequest, "2000-01-01")

  val createInvalidKnownFactsRequest = CreateKnownFactsRequest(invalidRegRequest, "2000-01-01")

  "createAgentKnownFacts" should {
    "return Right(()))" in {
      createAgentKnownFactsValid(AgentReference("HX2001"))
      await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("HX2001"))) mustBe Right(())
    }

    "return Left when AgentId is invalid" in {
      createAgentKnownFactsInvalidAgentId(AgentReference("ZZ0000"))
      await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("ZZ0000"))).isLeft mustBe true
    }

    "return Left when regime is not PAYE" in {
      createAgentKnownFactsInvalidRegime(AgentReference("HX2001"))
      await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("HX2001"), "AAA")).isLeft mustBe true
    }

    "return Left when AgentId is invalid and the regime is not PAYE" in {
      createAgentKnownFactsInvalidBoth
      await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("ZZ0000"), "AAA")).isLeft mustBe true
    }

    "return Left when payload is invalid" in {
      createAgentKnownFactsInvalidPayload(AgentReference("HX2001"))
      await(connector.createAgentKnownFacts(createInvalidKnownFactsRequest, AgentReference("HX2001"))).isLeft mustBe true
    }

    "return Left when DES is failing" in {
      createAgentKnownFactsFailsWithStatus(AgentReference("HX2001"), 503)
      await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("HX2001"))).isLeft mustBe true
    }
  }
}

