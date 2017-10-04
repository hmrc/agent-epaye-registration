package uk.gov.hmrc.agentepayeregistration.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.agentepayeregistration.audit.AgentEpayeRegistrationEvent.AgentEpayeRegistrationEvent

trait DataStreamStub extends Eventually {

  override implicit val patienceConfig = PatienceConfig(scaled(Span(5,Seconds)), scaled(Span(500,Millis)))

  def verifyAuditRequestSent(count: Int, event: AgentEpayeRegistrationEvent, tags: Map[String, String] = Map.empty, detail: Map[String, String] = Map.empty) = {
    eventually {
      verify(count, postRequestedFor(urlPathEqualTo(auditUrl))
        .withRequestBody(similarToJson(
          s"""{
              |  "auditSource": "agent-epaye-registration",
              |  "auditType": "$event",
              |  "tags": ${Json.toJson(tags)},
              |  "detail": ${Json.toJson(detail)}
              |}"""
        ))
      )
    }
  }

  def verifyAuditRequestNotSent(event: AgentEpayeRegistrationEvent) = {
    eventually {
      verify(0, postRequestedFor(urlPathEqualTo(auditUrl))
        .withRequestBody(similarToJson(
          s"""{
              |  "auditSource": "agent-epaye-registration",
              |  "auditType": "$event"
              |}"""
        ))
      )
    }
  }

  def givenAuditConnector() = {
    stubFor(post(urlPathEqualTo(auditUrl)).willReturn(aResponse().withStatus(200)))
  }

  private def auditUrl = "/write/audit"

  private def similarToJson(value: String) = equalToJson(value.stripMargin, true, true)

}
