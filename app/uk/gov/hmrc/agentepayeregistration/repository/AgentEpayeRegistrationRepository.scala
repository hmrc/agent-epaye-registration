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

package uk.gov.hmrc.agentepayeregistration.repository

import javax.inject.{Inject, Singleton}

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, RegistrationDetails, RegistrationRequest}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationRepository @Inject()(mongo: ReactiveMongoComponent)
  extends ReactiveRepository[RegistrationDetails, BSONObjectID](
    "agent-epaye-registration-record",
    mongo.mongoConnector.db,
    RegistrationDetails.registrationDetailsFormat,
    ReactiveMongoFormats.objectIdFormats) {


  override def indexes: Seq[Index] =
    Seq(Index(key = Seq("agentReference" -> IndexType.Ascending), name = Some("agentRefIndex"), unique = true),
      Index(key = Seq("createdDateTime" -> IndexType.Ascending), name = Some("createdDateTime"), unique = false))

  val initialAgentReference: String = "HX2000"

  def create(request: RegistrationRequest, createdDate: DateTime = DateTime.now(DateTimeZone.UTC))
            (implicit ec: ExecutionContext): Future[AgentReference] = {
    val mongoCodeDuplicateKey: Int = 11000

    for {
      maybeRegDetails <- collection.find(obj()).sort(obj("agentReference" -> -1)).one[RegistrationDetails]
      nextAgentRef = maybeRegDetails match {
        case Some(regDetails) => regDetails.agentReference.newReference
        case None => AgentReference(initialAgentReference)
      }
      _ <- insert(RegistrationDetails(nextAgentRef, request, createdDate)) recover {
        case error: DatabaseException if error.code.contains(mongoCodeDuplicateKey) => create(request)
      }
    } yield nextAgentRef
  }

  def findRegistrations(dateTimeFrom: DateTime, dateTimeTo: DateTime)
                       (implicit ec: ExecutionContext): Future[List[RegistrationDetails]] = {
    if(dateTimeTo.isBefore(dateTimeFrom)) throw new IllegalArgumentException("to date is before from date")

    val queryFilter: play.api.libs.json.JsObject = obj(
      "createdDateTime" -> obj(
        "$gte" -> obj("$date" -> dateTimeFrom.getMillis),
        "$lte" -> obj("$date" -> dateTimeTo.getMillis)
      )
    )

    collection.find(queryFilter)
      .sort(obj("createdDateTime" -> 1))
      .cursor[RegistrationDetails]()
      .collect[List](Integer.MAX_VALUE)
  }
}
