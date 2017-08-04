package uk.gov.hmrc.agentepayeregistration.repository

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.{CommandError, LastError}
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
    Seq(Index(key = Seq("agentReference" -> IndexType.Ascending), name = Some("agentRefIndex"), unique = true))

  val initialAgentReference = "HX2000"

  def create(request: RegistrationRequest)(implicit ec: ExecutionContext): Future[AgentReference] = {
    val mongoCodeDuplicateKey = 11000

    for {
      maybeRegDetails <- collection.find(obj()).sort(obj("agentReference" -> -1)).one[RegistrationDetails]
      nextAgentRef = maybeRegDetails match {
        case Some(regDetails) => regDetails.agentReference.newReference
        case None => AgentReference(initialAgentReference)
      }
      _ <- insert(RegistrationDetails(nextAgentRef, request)) recover {
        case error: DatabaseException if error.code == Some(mongoCodeDuplicateKey) => create(request)
      }
    } yield nextAgentRef
  }
}
