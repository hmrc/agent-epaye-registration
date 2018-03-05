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

package uk.gov.hmrc.agentepayeregistration.repository

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.stream.scaladsl.Source
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json.{obj, toJsFieldJsValueWrapper}
import play.api.libs.streams.Streams
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json.ImplicitBSONHandlers._
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
      Index(key = Seq("createdDateTime" -> IndexType.Ascending), name = Some("createdDateTimeIndex"), unique = false))

  val initialAgentReference: String = "HX2000"

  def create(request: RegistrationRequest, createdDate: DateTime = DateTime.now(DateTimeZone.UTC))
            (implicit ec: ExecutionContext): Future[RegistrationDetails] = {
    val mongoCodeDuplicateKey: Int = 11000

    for {
      maybeRegDetails <- collection.find(obj()).sort(obj("agentReference" -> -1)).one[RegistrationDetails]
      regDetails = {
        val nextAgentRef = maybeRegDetails match {
          case Some(regDetails) => regDetails.agentReference.newReference
          case None => AgentReference(initialAgentReference)
        }
        RegistrationDetails(nextAgentRef, request, createdDate)
      }
      _ <- insert(regDetails) recover {
          case error: DatabaseException if error.code.contains(mongoCodeDuplicateKey) => create(request)
        }
    } yield regDetails
  }

  def enumerateRegistrations(dateTimeFrom: DateTime, dateTimeTo: DateTime)
                            (implicit ec: ExecutionContext): Enumerator[RegistrationDetails] = {
    require(!dateTimeTo.isBefore(dateTimeFrom), "to date is before from date")

    val queryFilter = obj(
      "createdDateTime" -> obj(
        "$gte" -> obj("$date" -> dateTimeFrom.getMillis),
        "$lte" -> obj("$date" -> dateTimeTo.getMillis)
      )
    )

    collection.find(queryFilter)
      .sort(obj("createdDateTime" -> 1))
      .cursor[RegistrationDetails]()
      .enumerate(stopOnError = true)
  }

  def countRecords(dateTimeFrom: DateTime, dateTimeTo: DateTime)(implicit ec: ExecutionContext): Future[Int] = {
    require(!dateTimeTo.isBefore(dateTimeFrom), "to date is before from date")
    val queryFilter = obj(
      "createdDateTime" -> obj(
        "$gte" -> obj("$date" -> dateTimeFrom.getMillis),
        "$lte" -> obj("$date" -> dateTimeTo.getMillis)
      )
    )
    collection.count(Option(queryFilter))
  }

  def sourceRegistrations(dateTimeFrom: DateTime, dateTimeTo: DateTime)
                          (implicit ec: ExecutionContext): Source[RegistrationDetails, NotUsed] = {
    val enumerator = enumerateRegistrations(dateTimeFrom, dateTimeTo)
    val publisher = Streams.enumeratorToPublisher(enumerator)

    Source.fromPublisher(publisher)
  }
}
