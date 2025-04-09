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

package uk.gov.hmrc.agentepayeregistration.controllers

import cats.data.Validated.{Invalid, Valid}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.agentepayeregistration.audit.AuditService
import uk.gov.hmrc.agentepayeregistration.models.RegistrationRequest
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService
import uk.gov.hmrc.agentepayeregistration.validators.AgentEpayeRegistrationValidator.validateRegistrationRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationController @Inject() (
    registrationService: AgentEpayeRegistrationService,
    auditService: AuditService,
    cc: ControllerComponents
) extends BackendController(cc)
    with Logging {

  implicit val ec: ExecutionContext = cc.executionContext

  val register: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[RegistrationRequest]
      .map { registrationRequest =>
        validateRegistrationRequest(registrationRequest) match {

          case Invalid(failure) =>
            Future.successful(BadRequest(Json.toJson(failure)))

          case Valid(_) =>
            registrationService.register(registrationRequest).map {
              case Right(agentReference) =>
                auditService.sendAgentEpayeRegistrationRecordCreated(registrationRequest, agentReference)
                Ok(Json.toJson(agentReference))
              case Left(error) =>
                BadGateway(Json.toJson(error))
            }

        }
      }
      .recoverTotal(_ => Future.successful(BadRequest))
  }

}
