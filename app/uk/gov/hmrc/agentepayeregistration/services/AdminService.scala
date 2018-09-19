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

import javax.inject.Inject
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.UpdateWriteResult
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository

import scala.concurrent.{ExecutionContext, Future}

class AdminServiceImpl @Inject()(mongo: ReactiveMongoComponent, val mongoRepository: AgentEpayeRegistrationRepository) extends AdminService

trait AdminService {

  val mongoRepository: AgentEpayeRegistrationRepository

  def deleteStaleDocuments()(implicit ec: ExecutionContext): Future[List[UpdateWriteResult]] = {
    mongoRepository.findStaleReferenceFields(5) flatMap { documents =>
      Future.sequence(documents map { document =>
        Logger.info(s"$document was updated")
        mongoRepository.removeRedundantFields(document.value)
      })
    }
  }
}