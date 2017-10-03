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

package uk.gov.hmrc.agentepayeregistration.audit

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import play.api.test.FakeRequest
import uk.gov.hmrc.agentepayeregistration.models.{Address, AgentReference, RegistrationRequest}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{AuditEvent, DataEvent}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.{Authorization, RequestId, SessionId}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

class AuditServiceSpec extends UnitSpec with MockitoSugar with Eventually {

  override implicit val patienceConfig = PatienceConfig(
    timeout = scaled(Span(500, Millis)),
    interval = scaled(Span(200, Millis)))

  "auditEvent" should {
    "send an AgentEpayeRegistrationRecordCreated event with the correct fields" in {
      val mockConnector = mock[AuditConnector]
      val service = new AuditService(mockConnector)


      val hc = HeaderCarrier(
        authorization = Some(Authorization("dummy bearer token")),
        sessionId = Some(SessionId("dummy session id")),
        requestId = Some(RequestId("dummy request id"))
      )

      val agentReference = AgentReference("HX2345")
      val registrationRequest = RegistrationRequest(
        agentName = "John Smith",
        contactName = "John Anderson Smith",
        telephoneNumber = Some("12313"),
        faxNumber = Some("1234567"),
        emailAddress = Some("john.smith@email.com"),
        address = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), "postCode" )
      )

      await(service.sendAgentEpayeRegistrationRecordCreated(
        registrationRequest,
        agentReference)(
        hc,
        FakeRequest("GET", "/path")
      ))

      eventually {
        val captor = ArgumentCaptor.forClass(classOf[AuditEvent])
        verify(mockConnector).sendEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
        captor.getValue shouldBe an[DataEvent]
        val sentEvent = captor.getValue.asInstanceOf[DataEvent]

        sentEvent.auditType shouldBe "AgentEpayeRegistrationRecordCreated"
        sentEvent.auditSource shouldBe "agent-epaye-registration"
        sentEvent.detail("agentReference") shouldBe "HX2345"
        sentEvent.detail("agentName") shouldBe "John Smith"
        sentEvent.detail("contactName") shouldBe "John Anderson Smith"
        sentEvent.detail("telephoneNumber") shouldBe "12313"
        sentEvent.detail("faxNumber") shouldBe "1234567"
        sentEvent.detail("emailAddress") shouldBe "john.smith@email.com"
        sentEvent.detail("address") shouldBe "addressLine1 addressLine2 addressLine3 addressLine4 postCode"

        sentEvent.tags.contains("Authorization") shouldBe false
        sentEvent.detail("Authorization") shouldBe "dummy bearer token"

        sentEvent.tags("transactionName") shouldBe "agent-epaye-registration-record-created"
        sentEvent.tags("path") shouldBe "/path"
        sentEvent.tags("X-Session-ID") shouldBe "dummy session id"
        sentEvent.tags("X-Request-ID") shouldBe "dummy request id"
      }
    }
  }

}
