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

package uk.gov.hmrc.agentepayeregistration.controllers

import javax.inject._

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.agentepayeregistration.connectors.AuthConnector
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, RegistrationExtraction, RegistrationRequest}
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService
import uk.gov.hmrc.auth.core.authorise.Enrolment
import uk.gov.hmrc.auth.core.retrieve.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, NoActiveSession}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class AgentEpayeRegistrationController @Inject()(@Named("extract.auth.stride.enrolment") strideEnrolment: String, service: AgentEpayeRegistrationService, val authConnector: AuthConnector) extends BaseController with AuthorisedFunctions {

  val register = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationRequest].map { details =>
      service.register(details).map {
        case Right(x) => Ok(Json.toJson(x))
        case Left(failure) => BadRequest(Json.toJson(failure))
      }
    }.recoverTotal(_ => Future.successful(BadRequest))
  }

  def extract(dateFrom: String, dateTo: String) = Action.async { implicit request =>
    authorised(Enrolment(strideEnrolment) and AuthProviders(PrivilegedApplication)) {
      service.extract(dateFrom, dateTo).map {
        case Right(registrations) => Ok(Json.obj("registrations" -> Json.toJson(registrations)))
        case Left(failure) => BadRequest(Json.toJson(failure))
      }.recoverWith {
        case ex: NoActiveSession =>
          Future.successful(Unauthorized(ex.getMessage))
        case ex: AuthorisationException =>
          Future.successful(Forbidden(ex.getMessage))
      }
    }
  }
}
