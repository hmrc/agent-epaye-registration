/*
 * Copyright 2020 HM Revenue & Customs
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

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import config.AppConfig
import javax.inject.Inject
import play.api.libs.json._
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, CreateKnownFactsRequest}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}

class DesConnector @Inject()(config: AppConfig,
                             http: DefaultHttpClient,
                             metrics: Metrics) extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def createAgentKnownFacts(knownFactDetails: CreateKnownFactsRequest, agentRef: AgentReference, regime: String="PAYE")(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    postWithDesHeaders[CreateKnownFactsRequest, HttpResponse]("createAgentKnownFactsAPI1337",
      new URL(s"${config.desURL}/agents/regime/$regime/agentid/${agentRef.value}/known-facts"),
      knownFactDetails)
  }

  private def postWithDesHeaders[A: Writes, B: HttpReads](apiName: String, url: URL, body: A)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[B] = {
    val desHeaderCarrier = hc.copy(
      authorization = Some(Authorization(s"Bearer ${config.desToken}")),
      extraHeaders = hc.extraHeaders :+ "Environment" -> config.desEnv)
      monitor(s"ConsumedAPI-DES-$apiName-POST") {
      http.POST[A, B](url.toString, body)(implicitly[Writes[A]], implicitly[HttpReads[B]], desHeaderCarrier, ec)
    }
  }
}
