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

import akka.util.ByteString
import org.joda.time.LocalDate
import play.api.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.iteratee.{Enumeratee, Enumerator}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Action
import uk.gov.hmrc.agentepayeregistration.connectors.AuthConnector
import uk.gov.hmrc.agentepayeregistration.models.RegistrationRequest
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService
import uk.gov.hmrc.auth.core.authorise.Enrolment
import uk.gov.hmrc.auth.core.retrieve.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, NoActiveSession}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationController @Inject()(@Named("extract.auth.stride.enrolment") strideEnrolment: String, service: AgentEpayeRegistrationService, val authConnector: AuthConnector) extends BaseController with AuthorisedFunctions {
  lazy val logger = Logger("registrationController")

  val register = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationRequest].map { details =>
      service.register(details).map {
        case Right(x) => Ok(Json.toJson(x))
        case Left(failure) => BadRequest(Json.toJson(failure))
      }
    }.recoverTotal(_ => Future.successful(BadRequest))
  }

  def extract(dateFrom: LocalDate, dateTo: LocalDate) = Action.async { implicit request =>
    authorised(Enrolment(strideEnrolment) and AuthProviders(PrivilegedApplication)) {
      service.extract(dateFrom, dateTo) match {
        case Right(enumeratorRegExtracts) => {
          val source = enumerateToJson(enumeratorRegExtracts, "registrations")

          Future.successful(Ok.feed(source).withHeaders((HeaderNames.CONTENT_TYPE, MimeTypes.JSON)))
        }
        case Left(failure) => Future.successful(BadRequest(Json.toJson(failure)))
      }
    }.recoverWith {
      case ex: NoActiveSession =>
        logger.warn("No active session whilst trying to extract registrations", ex)
        Future.successful(Unauthorized)
      case ex: AuthorisationException =>
        logger.warn("Authorisation exception whilst trying to extract registrations", ex)
        Future.successful(Forbidden)
    }
  }

  private def enumerateToJson[A](enumerator: Enumerator[A], key: String)(implicit writer: Writes[A], executionCtx: ExecutionContext) = {
    val jsonArrayItems = enumerator.map(item => Json.toJson(item).toString())
    val enumerateJson = Enumerator(s"""{"$key":[""")
      .andThen(jsonArrayItems.through(Enumeratee.take(1)))
      .andThen(jsonArrayItems.map("," + _))
      .andThen(Enumerator("]}"))
    enumerateJson.map(ByteString.apply)
  }
}
