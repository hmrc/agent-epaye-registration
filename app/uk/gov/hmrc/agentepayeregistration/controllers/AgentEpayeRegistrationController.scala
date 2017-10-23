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

import java.time.LocalDateTime
import javax.inject._

import akka.stream.scaladsl.{Concat, Source}
import akka.util.ByteString
import org.joda.time.LocalDate
import play.api.Logger
import play.api.http.HttpEntity.Streamed
import play.api.http.MimeTypes
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Action
import uk.gov.hmrc.agentepayeregistration.audit.AuditService
import uk.gov.hmrc.agentepayeregistration.connectors.AuthConnector
import uk.gov.hmrc.agentepayeregistration.models.RegistrationRequest
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.PAClientId
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.auth.core.retrieve.Retrievals.authProviderId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class AgentEpayeRegistrationController @Inject()(@Named("extract.auth.stride.enrolment") strideEnrolment: String,
                                                 registrationService: AgentEpayeRegistrationService,
                                                 val authConnector: AuthConnector,
                                                 auditService: AuditService) extends BaseController with AuthorisedFunctions {
  lazy val logger = Logger("registrationController")

  val register = Action.async(parse.json) { implicit request =>

    request.body.validate[RegistrationRequest].map { registrationRequest =>
      registrationService.register(registrationRequest).map {
        case Right(agentReference) => {

          auditService.sendAgentEpayeRegistrationRecordCreated(registrationRequest, agentReference)
          Ok(Json.toJson(agentReference))
        }
        case Left(failure) => BadRequest(Json.toJson(failure))
      }
    }.recoverTotal(_ => Future.successful(BadRequest))
  }

  protected def timeStamp: String = LocalDateTime.now().toString

  def extract(dateFrom: LocalDate, dateTo: LocalDate) = Action.async { implicit request =>
    authorised(Enrolment(strideEnrolment) and AuthProviders(PrivilegedApplication)).retrieve(authProviderId) {
      case authProviderId: PAClientId =>
        registrationService.extract(dateFrom, dateTo) match {
          case Right((sourceRegExtracts, count)) => {
            val streamedEntity = Streamed(sourceToJson(sourceRegExtracts, "registrations"), None, Some(MimeTypes.JSON))
            count.map( records =>
              auditService.sendAgentEpayeRegistrationExtract(authProviderId.clientId, timeStamp, dateFrom.toString(), dateTo.toString(), records)
            ).flatMap( _ =>
              Future.successful(Ok.sendEntity(streamedEntity))
            )
          }
          case Left(failure) => Future.successful(BadRequest(Json.toJson(failure)))
        }
        //This part should never happen. This is to satisfy scala pattern match exhaustive check in order to compile.
      case _ => Future.successful(Forbidden)
    }.recoverWith {
      case ex: NoActiveSession =>
        logger.warn("No active session whilst trying to extract registrations", ex)
        Future.successful(Unauthorized)
      case ex: AuthorisationException =>
        logger.warn("Authorisation exception whilst trying to extract registrations", ex)
        Future.successful(Forbidden)
    }
  }

  private def sourceToJson[A](source: Source[A, _], key: String)(implicit writer: Writes[A], executionCtx: ExecutionContext) = {
    val startJson = Source.single(s"""{ "$key" : [""")
    val endJson = Source.single("""], "complete": true }""")
    val jsonItems = source.map(extract => Json.toJson(extract).toString()).intersperse(",")
    val combinedJsonStr = Source.combine(startJson, jsonItems, endJson)(Concat(_))

    combinedJsonStr.map(ByteString.apply)
  }
}
