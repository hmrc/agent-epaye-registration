package uk.gov.hmrc.agentepayeregistration.stubs

import java.time.LocalDateTime

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.agentepayeregistration.audit.AgentEpayeRegistrationEvent.AgentEpayeRegistrationEvent

trait DataStreamStub extends Eventually {

  override implicit val patienceConfig = PatienceConfig(scaled(Span(5,Seconds)), scaled(Span(500,Millis)))

  //2017-10-23T12:30:51.284

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

  def verifyAuditRequestSentWithExtractDate(count: Int) = {
    val iso8601Regex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}"
    eventually {
      verify(count, postRequestedFor(urlPathEqualTo(auditUrl))
        .withRequestBody(
          matchingJsonPath("$.detail.extractDate", matching(iso8601Regex))
        )
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

//  private def compareDateValue(value: String) =

  private def similarToJson(value: String) = equalToJson(value.stripMargin, true, true)

}
