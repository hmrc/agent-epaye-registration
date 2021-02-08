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

package uk.gov.hmrc.agentepayeregistration.controllers

import config.AppConfig
import javax.inject._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.agentepayeregistration.audit.AuditService
import uk.gov.hmrc.agentepayeregistration.models.RegistrationRequest
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AgentEpayeRegistrationController @Inject()(registrationService: AgentEpayeRegistrationService,
                                                 cc: ControllerComponents,
                                                 config: AppConfig,
                                                 auditService: AuditService) extends BackendController(cc) {
  lazy val logger = Logger("registrationController")

  val register: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationRequest].map { registrationRequest =>
      registrationService.register(registrationRequest).map {
        case Right(agentReference) =>
          auditService.sendAgentEpayeRegistrationRecordCreated(registrationRequest, agentReference)
          Ok(Json.toJson(agentReference))
        case Left(failure) => BadRequest(Json.toJson(failure))
      }
    }.recoverTotal(_ => Future.successful(BadRequest))
  }
}
