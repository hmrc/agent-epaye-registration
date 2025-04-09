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

package uk.gov.hmrc.agentepayeregistration.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.agentepayeregistration.audit.AgentEpayeRegistrationEvent.AgentEpayeRegistrationEvent

trait DataStreamStub extends Eventually {

  override implicit val patienceConfig = PatienceConfig(scaled(Span(5, Seconds)), scaled(Span(500, Millis)))

  def verifyAuditRequestSent(
      count: Int,
      event: AgentEpayeRegistrationEvent,
      tags: Map[String, String] = Map.empty,
      detail: Map[String, String] = Map.empty
  ) =
    eventually {
      verify(
        count,
        postRequestedFor(urlPathEqualTo(auditUrl))
          .withRequestBody(
            similarToJson(
              s"""{
              |  "auditSource": "agent-epaye-registration",
              |  "auditType": "$event",
              |  "tags": ${Json.toJson(tags)},
              |  "detail": ${Json.toJson(detail)}
              |}"""
            )
          )
      )
    }

  def verifyAuditRequestSentWithExtractDate(count: Int) = {
    val iso8601Regex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}"
    eventually {
      verify(
        count,
        postRequestedFor(urlPathEqualTo(auditUrl))
          .withRequestBody(
            matchingJsonPath("$.detail.extractDate", matching(iso8601Regex))
          )
      )
    }
  }

  def verifyAuditRequestNotSent(event: AgentEpayeRegistrationEvent) =
    eventually {
      verify(
        0,
        postRequestedFor(urlPathEqualTo(auditUrl))
          .withRequestBody(
            similarToJson(
              s"""{
              |  "auditSource": "agent-epaye-registration",
              |  "auditType": "$event"
              |}"""
            )
          )
      )
    }

  def givenAuditConnector() =
    stubFor(post(urlPathEqualTo(auditUrl)).willReturn(aResponse().withStatus(200)))

  private def auditUrl = "/write/audit"

  private def similarToJson(value: String) = equalToJson(value.stripMargin, true, true)

}
