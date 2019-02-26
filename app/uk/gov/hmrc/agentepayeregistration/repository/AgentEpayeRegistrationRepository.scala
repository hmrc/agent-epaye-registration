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

package uk.gov.hmrc.agentepayeregistration.repository

import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Json.{obj, toJsFieldJsValueWrapper}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor
import reactivemongo.api.commands.UpdateWriteResult
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
    "agent-epaye-registration-record",
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
      oAgentRef <- collection.find(obj()).sort(obj("agentReference" -> -1)).one[AgentReference]
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
      Logger.error(s"[removeStaleDocuments] Mongo failed, problem occured in collect - ex: ${ex.getMessage}")
    )
    val ascending = Json.obj("agentReference" -> 1)

    collection.find(query)
      .sort(ascending)
      .batchSize(count)
      .cursor[AgentReference]()
      .collect[List](count, logOnError)
  }

def removeRedundantFields(id: String)(implicit ec: ExecutionContext): Future[Boolean] = {

    val selector = BSONDocument("agentReference" -> id)

    val modifier = BSONDocument(
      "$unset" -> BSONDocument(
        "agentName" -> 1,
        "contactName" -> 1,
        "telephoneNumber" -> 1,
        "emailAddress" -> 1,
        "address" -> 1,
        "createdDateTime" -> 1
      )
    )
    collection.update(selector,modifier) map {
      Logger.info(s"AgentReference: $id had stale fields pruned")
      _.ok
    }
  }
}
