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

package uk.gov.hmrc.agentepayeregistration.services

import javax.inject.{Inject, Singleton}

import cats.data.Validated.{Invalid, Valid}
import uk.gov.hmrc.agentepayeregistration.models._
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.agentepayeregistration.validators.AgentEpayeRegistrationValidator._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationService @Inject()(repository: AgentEpayeRegistrationRepository) {

  def register(request: RegistrationRequest)(implicit ec: ExecutionContext): Future[Either[Failure, AgentReference]] = {
    validateRegistrationRequest(request) match {
      case Valid(_) => repository.create(request).map(Right(_))
      case Invalid(failure) => Future.successful(Left(failure))
    }
  }

  def extract(dateFrom: String, dateTo: String)
             (implicit ec: ExecutionContext): Future[Either[Failure, List[RegistrationExtraction]]] = {
    validateDateRange(dateFrom, dateTo) match {
      case Valid(_) =>
        val startOfFromDay = parseISODate(dateFrom).toDateTimeAtStartOfDay
        val endOfToDay = parseISODate(dateTo).toDateTimeAtStartOfDay.plusDays(1).minusMillis(1)
        repository.findRegistrations(startOfFromDay, endOfToDay).map { registrations =>
          Right(registrations.map(RegistrationExtraction.apply))
        }
      case Invalid(failure) =>
        Future.successful(Left(failure))
    }
  }
}
