package uk.gov.hmrc.agentepayeregistration.repository

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
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
    Seq(Index(key = Seq("agentReference" -> IndexType.Ascending), name = Some("agentRefIndex"), unique = true))

  def create(request: RegistrationRequest)(implicit ec: ExecutionContext): Future[AgentReference] = {
    for {
      agentRef <- collection.find(Json.obj()).sort(Json.obj("$natural" -> -1)).one[RegistrationDetails].map(_.map(_.agentReference))
      nextAgentRef = agentRef match {
        case Some(x) => x.newReference
        case None => AgentReference("HX0001")
      }
      _ <- insert(RegistrationDetails(nextAgentRef, request))
    } yield nextAgentRef
  }
}
