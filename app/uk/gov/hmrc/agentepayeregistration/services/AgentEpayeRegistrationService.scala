/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.agentepayeregistration.services

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.agentepayeregistration.audit.AuditService
import uk.gov.hmrc.agentepayeregistration.connectors.DesConnector
import uk.gov.hmrc.agentepayeregistration.models._
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class AgentEpayeRegistrationService @Inject()(repository: AgentEpayeRegistrationRepository,
                                              desConnector: DesConnector,
                                              auditService: AuditService) extends Logging {

  def register(regRequest: RegistrationRequest)
              (implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[Any]): Future[Either[String, AgentReference]] = {
    for {
      regDetails <- repository.create(regRequest)
      currentDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now)
      knownFactsCreated <- desConnector.createAgentKnownFacts(CreateKnownFactsRequest(regRequest, currentDate), regDetails.agentReference).andThen {
        case Success(response) if response.isRight =>
          auditService.sendAgentKnownFactsCreated(regDetails)
        case _ =>
      }
    } yield {
      knownFactsCreated match {
        case Right(()) => Right(regDetails.agentReference)
        case Left(error) => Left(error)
      }

    }
  }
}
