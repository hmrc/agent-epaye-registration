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

package uk.gov.hmrc.agentepayeregistration.jobs

import javax.inject.{Inject, Singleton}
import org.joda.time.Duration
import play.api.Logger
import play.modules.reactivemongo.MongoDbConnection
import uk.gov.hmrc.lock.{LockKeeper, LockRepository}
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.agentepayeregistration.services.AdminService

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class RemoveStaleReferenceFieldsImpl @Inject()(val mongoRepository: AgentEpayeRegistrationRepository,
                                               val adminService: AdminService) extends RemoveStaleReferenceFields {
  val name: String = "remove-stale-reference-fields-jobs"
  val mongoRepo: AgentEpayeRegistrationRepository = mongoRepository
  override lazy val lock: LockKeeper = new LockKeeper() {
    override val lockId = s"$name-lock"
    override val forceLockReleaseAfter: Duration = lockTimeout
    private implicit val mongo = new MongoDbConnection {}.db
    override val repo = new LockRepository
  }
}

trait RemoveStaleReferenceFields extends ExclusiveScheduledJob with JobConfig with AdminService {

  val lock: LockKeeper
  val mongoRepo: AgentEpayeRegistrationRepository
  val adminService: AdminService

  override def executeInMutex(implicit ec: ExecutionContext) = {

    if (deleteStaleDocuments().isCompleted) {
      lock.tryLock {
        adminService.deleteStaleDocuments() map { deletions =>
          Result(s"Successfully deleted ${deletions.size} stale documents")
        }
      } map {
        case Some(x) =>
          Logger.info(s"successfully acquired lock for $name")
          x
        case None =>
          Logger.info(s"failed to acquire lock for $name")
          Result(s"$name failed")
      } recover {
        case _: Exception => Result(s"$name failed")
      }
    } else {
      Future.successful(Result("Successfully deleted stale documents"))
    }
  }
}
