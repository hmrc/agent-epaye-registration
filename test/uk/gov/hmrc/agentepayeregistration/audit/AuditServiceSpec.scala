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

package uk.gov.hmrc.agentepayeregistration.audit

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import play.api.test.FakeRequest
import uk.gov.hmrc.agentepayeregistration.models.{Address, AgentReference, RegistrationRequest}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, RequestId, SessionId}

class AuditServiceSpec extends PlaySpec with MockitoSugar with Eventually {

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
        address = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), "postCode")
      )

      await(service.sendAgentEpayeRegistrationRecordCreated(
        registrationRequest,
        agentReference)(
        hc,
        FakeRequest("GET", "/path")
      ))

      eventually {
        val captor = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(mockConnector).sendEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
        val sentEvent = captor.getValue.asInstanceOf[DataEvent]

        sentEvent.auditType mustBe "AgentEpayeRegistrationRecordCreated"
        sentEvent.auditSource mustBe "agent-epaye-registration"
        sentEvent.detail("payeAgentRef") mustBe "HX2345"
        sentEvent.detail("agentName") mustBe "John Smith"
        sentEvent.detail("contactName") mustBe "John Anderson Smith"
        sentEvent.detail("telephoneNumber") mustBe "12313"
        sentEvent.detail("faxNumber") mustBe "1234567"
        sentEvent.detail("emailAddress") mustBe "john.smith@email.com"
        sentEvent.detail("addressLine1") mustBe "addressLine1"
        sentEvent.detail("addressLine2") mustBe "addressLine2"
        sentEvent.detail("addressLine3") mustBe "addressLine3"
        sentEvent.detail("addressLine4") mustBe "addressLine4"
        sentEvent.detail("postcode") mustBe "postCode"

        sentEvent.tags.contains("Authorization") mustBe false

        sentEvent.tags("transactionName") mustBe "Agent ePAYE registration created"
        sentEvent.tags("path") mustBe "/path"
        sentEvent.tags("X-Session-ID") mustBe "dummy session id"
        sentEvent.tags("X-Request-ID") mustBe "dummy request id"
      }
    }

    "send an AgentEpayeRegistrationRecordCreated event with the correct and without optional fields" in {
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
        telephoneNumber = None,
        faxNumber = None,
        emailAddress = None,
        address = Address("addressLine1", "addressLine2", None, None, "postCode")
      )

      await(service.sendAgentEpayeRegistrationRecordCreated(
        registrationRequest,
        agentReference)(
        hc,
        FakeRequest("GET", "/path")
      ))

      eventually {
        val captor = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(mockConnector).sendEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
        val sentEvent = captor.getValue.asInstanceOf[DataEvent]

        sentEvent.auditType mustBe "AgentEpayeRegistrationRecordCreated"
        sentEvent.auditSource mustBe "agent-epaye-registration"
        sentEvent.detail("payeAgentRef") mustBe "HX2345"
        sentEvent.detail("agentName") mustBe "John Smith"
        sentEvent.detail("contactName") mustBe "John Anderson Smith"
        sentEvent.detail("addressLine1") mustBe "addressLine1"
        sentEvent.detail("addressLine2") mustBe "addressLine2"
        sentEvent.detail("postcode") mustBe "postCode"

        sentEvent.tags.contains("Authorization") mustBe false

        sentEvent.tags("transactionName") mustBe "Agent ePAYE registration created"
        sentEvent.tags("path") mustBe "/path"
        sentEvent.tags("X-Session-ID") mustBe "dummy session id"
        sentEvent.tags("X-Request-ID") mustBe "dummy request id"
      }
    }

    "send an AgentEpayeRegistrationRecordExtract event with the correct fields" in {
      val mockConnector = mock[AuditConnector]
      val service = new AuditService(mockConnector)


      val hc = HeaderCarrier(
        authorization = Some(Authorization("dummy bearer token")),
        sessionId = Some(SessionId("dummy session id")),
        requestId = Some(RequestId("dummy request id"))
      )

      await(service.sendAgentEpayeRegistrationExtract(
        "userId", "extractDate", "dateFrom", "dateTo", 2)(
        hc,
        FakeRequest("GET", "/path")
      ))

      eventually {
        val captor = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(mockConnector).sendEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
        val sentEvent = captor.getValue.asInstanceOf[DataEvent]

        sentEvent.auditType mustBe "AgentEpayeRegistrationExtract"
        sentEvent.auditSource mustBe "agent-epaye-registration"
        sentEvent.detail("strideUserId") mustBe "userId"
        sentEvent.detail("extractDate") mustBe "extractDate"
        sentEvent.detail("dateFrom") mustBe "dateFrom"
        sentEvent.detail("dateTo") mustBe "dateTo"
        sentEvent.detail("recordCount") mustBe "2"

        sentEvent.tags.contains("Authorization") mustBe false

        sentEvent.tags("transactionName") mustBe "agent-epaye-registration-extract"
        sentEvent.tags("path") mustBe "/path"
        sentEvent.tags("X-Session-ID") mustBe "dummy session id"
        sentEvent.tags("X-Request-ID") mustBe "dummy request id"
      }
    }
  }
}
