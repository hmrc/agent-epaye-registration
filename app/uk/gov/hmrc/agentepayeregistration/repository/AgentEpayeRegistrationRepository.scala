/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.Json.{obj, toJsFieldJsValueWrapper}
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, RegistrationDetails, RegistrationRequest}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationRepository @Inject()(mongo: ReactiveMongoComponent)
  extends ReactiveRepository[AgentReference, BSONObjectID](
    collectionName = "agent-epaye-registration-record",
    mongo.mongoConnector.db,
    AgentReference.mongoFormat,
    ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] =
    Seq(Index(key = Seq("agentReference" -> IndexType.Ascending), name = Some("agentRefIndex"), unique = true))

  val initialAgentReference: String = "HX2000"

  def create(request: RegistrationRequest, createdDate: DateTime = DateTime.now(DateTimeZone.UTC))
            (implicit ec: ExecutionContext): Future[RegistrationDetails] = {
    val mongoCodeDuplicateKey: Int = 11000

    for {
      oAgentRef <- collection.find(obj(), Option.empty[JsObject]).sort(obj("agentReference" -> -1)).one[AgentReference]
      regDetails = {
        val nextAgentRef = oAgentRef match {
          case Some(ref) => ref.newReference
          case None => AgentReference(initialAgentReference)
        }
        RegistrationDetails(nextAgentRef, request, createdDate)
      }
      _ <- insert(regDetails.agentReference) recover {
        case error: DatabaseException if error.code.contains(mongoCodeDuplicateKey) => create(request)
      }
    } yield regDetails
  }

  def findStaleReferenceFields(count: Int)(implicit ec: ExecutionContext): Future[List[AgentReference]] = {

    val query = BSONDocument("$or" -> Json.arr(
      BSONDocument("agentName" -> Json.obj("$exists" -> true)),
      BSONDocument("contactName" -> Json.obj("$exists" -> true)),
      BSONDocument("telephoneNumber" -> Json.obj("$exists" -> true)),
      BSONDocument("emailAddress" -> Json.obj("$exists" -> true)),
      BSONDocument("address" -> Json.obj("$exists" -> true)),
      BSONDocument("createdDateTime" -> Json.obj("$exists" -> true))
    ))

    val logOnError = Cursor.ContOnError[List[AgentReference]]((_, ex) =>
      logger.error(s"[removeStaleDocuments] Mongo failed, problem occurred in collect - ex: ${ex.getMessage}")
    )
    val ascending = Json.obj("agentReference" -> 1)

    collection.find(query, Option.empty[JsObject])
      .sort(ascending)
      .batchSize(count)
      .cursor[AgentReference]()
      .collect[List](count, logOnError)
  }

}
