/*
 * Copyright 2018 HM Revenue & Customs
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

import java.net.URL
import javax.inject.{Inject, Named}

import com.codahale.metrics.MetricRegistry
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, CreateKnownFactsRequest}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost, HttpReads, HttpResponse}
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import com.kenshoo.play.metrics.Metrics

import scala.concurrent.{ExecutionContext, Future}

class DesConnector @Inject()(@Named("des-baseUrl") odsBaseUrl: URL,
                             @Named("microservice.services.des.authorization-token") authorizationToken: String,
                             @Named("microservice.services.des.environment") environment: String,
                             http: HttpPost,
                             metrics: Metrics) extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  private def createAgentKnownFactsJson(knownFactDetails: CreateKnownFactsRequest) = Json.parse(
  s"""{
      "agentName": "${knownFactDetails.regRequest.agentName}",
      "contactName": "${knownFactDetails.regRequest.contactName}",
      "addressLine1": "${knownFactDetails.regRequest.address.addressLine1}",
      "addressLine2": "${knownFactDetails.regRequest.address.addressLine2}",
      "postCode": "${knownFactDetails.regRequest.address.postCode}",
      "phoneNo": "${knownFactDetails.regRequest.phoneNo.getOrElse("")}",
      "faxNumber": "${knownFactDetails.regRequest.faxNumber.getOrElse("")}",
      "email": "${knownFactDetails.regRequest.email.getOrElse("")}",
      "createdDate": "${knownFactDetails.createdDate}"
  }""")

  def createAgentKnownFacts(knownFactDetails: CreateKnownFactsRequest, agentRef: AgentReference, regime: String="PAYE")(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    postWithDesHeaders("DES",
      new URL(s"$odsBaseUrl/agents/regime/$regime/agentid/${agentRef.value}/known-facts"),
      createAgentKnownFactsJson(knownFactDetails))
  }

  private def postWithDesHeaders[A: Writes, B: HttpReads](apiName: String, url: URL, body: A)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[B] = {
    val desHeaderCarrier = hc.copy(
      authorization = Some(Authorization(s"Bearer $authorizationToken")),
      extraHeaders = hc.extraHeaders :+ "Environment" -> environment)
      monitor(s"ConsumedAPI-DES-$apiName-POST") {
      http.POST[A, B](url.toString, body)(implicitly[Writes[A]], implicitly[HttpReads[B]], desHeaderCarrier, ec)
    }
  }
}
