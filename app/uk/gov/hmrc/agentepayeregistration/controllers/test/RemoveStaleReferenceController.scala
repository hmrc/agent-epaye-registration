/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.agentepayeregistration.controllers.test

import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.agentepayeregistration.services.AdminService
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

class RemoveStaleReferenceControllerImpl @Inject()(val mongoRepository: AgentEpayeRegistrationRepository) extends RemoveStaleReferenceController

trait RemoveStaleReferenceController extends BaseController with AdminService {

  def mongo(): Action[AnyContent] = Action.async {
    implicit request =>
      deleteStaleDocuments().map { res =>
        if (res.contains(false)) Logger.error("[RemoveStaleReferenceController] Failed to delete data from all retrieved documents")
        Ok
      }
  }
}



