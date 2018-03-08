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

package uk.gov.hmrc.agentepayeregistration.services

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.stream.scaladsl.Source
import cats.data.Validated.{Invalid, Valid}
import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.agentepayeregistration.audit.AuditService
import uk.gov.hmrc.agentepayeregistration.connectors.DesConnector
import uk.gov.hmrc.agentepayeregistration.models._
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.agentepayeregistration.validators.AgentEpayeRegistrationValidator._
import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class AgentEpayeRegistrationService @Inject()(repository: AgentEpayeRegistrationRepository,
                                              desConnector: DesConnector,
                                              auditService : AuditService) {

  def register(regRequest: RegistrationRequest)
              (implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[Any]): Future[Either[Failure, AgentReference]] = {
      validateRegistrationRequest(regRequest) match {
        case Valid(_) => {
          for {
            regDetails <- repository.create(regRequest)
            currentDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now)
            _ <- desConnector.createAgentKnownFacts(CreateKnownFactsRequest(regRequest, currentDate), regDetails.agentReference).andThen {
              case Success(_) => auditService.sendAgentKnownFactsCreated(regDetails)
            }
          } yield Right(regDetails.agentReference)

        }
        case Invalid(failure) => Future.successful(Left(failure))
      }
    }

  def extract(dateFrom: LocalDate, dateTo: LocalDate)
             (implicit ec: ExecutionContext): Future[Either[Failure, ExtractedRegistrations]] = {
    validateDateRange(dateFrom, dateTo) match {
      case Valid(_) =>
        val startOfDayFrom = dateFrom.toDateTimeAtStartOfDay
        val endOfDayTo = dateTo.toDateTimeAtStartOfDay.plusDays(1).minusMillis(1)

        for {
          count <- repository.countRecords(startOfDayFrom, endOfDayTo)
        } yield count match {
          case 0 =>
            Right(ExtractedRegistrations(Source.empty, 0))
          case count =>
            val sourceRegDetails = repository.sourceRegistrations(startOfDayFrom, endOfDayTo)
            val sourceRegExtractions = sourceRegDetails.map(RegistrationExtraction.apply)

            Right(ExtractedRegistrations(sourceRegExtractions, count))
        }
      case Invalid(failure) =>
        Future.successful(Left(failure))
    }
  }
}

case class ExtractedRegistrations(stream: Source[RegistrationExtraction, NotUsed], count: Int)
