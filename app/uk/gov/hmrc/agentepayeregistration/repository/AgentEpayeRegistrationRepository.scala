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

package uk.gov.hmrc.agentepayeregistration.repository

import javax.inject.{Inject, Singleton}
import java.time.OffsetDateTime
import org.mongodb.scala.MongoException
import org.mongodb.scala.model.Filters.equal
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, RegistrationDetails, RegistrationRequest}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala.model._
import play.api.{Configuration, Logging}

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AgentEpayeRegistrationRepository @Inject()(mongo: MongoComponent, config: Configuration)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[AgentReference](
    mongoComponent = mongo,
    collectionName = "agent-epaye-registration-record",
    domainFormat = AgentReference.mongoFormat,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("agentReference"),
        IndexOptions()
          .name("agentRefIndex")
          .unique(true)
      )
    )
  ) with Logging {

  // Agent references persist indefinitely, so no TTL
  override lazy val requiresTtlIndex: Boolean = false

  val initialAgentReference: String = "HX2000"

  def create(request: RegistrationRequest, createdDate: OffsetDateTime = OffsetDateTime.now()): Future[RegistrationDetails] = {
    val mongoCodeDuplicateKey: Int = 11000

    for {
      oAgentRef <- collection.find[AgentReference]().sort(equal("agentReference", -1)).headOption()
      regDetails = {
        val nextAgentRef = oAgentRef match {
          case Some(ref) => ref.newReference
          case None => AgentReference(initialAgentReference)
        }
        RegistrationDetails(nextAgentRef, request, createdDate)
      }
      _ <- collection.insertOne(regDetails.agentReference).toFuture() recover {
        case error: MongoException  if error.getCode == mongoCodeDuplicateKey => create(request)
      }
    } yield regDetails
  }

}
