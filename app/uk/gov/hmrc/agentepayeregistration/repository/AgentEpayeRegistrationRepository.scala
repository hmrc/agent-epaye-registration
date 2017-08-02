package uk.gov.hmrc.agentepayeregistration.repository

import javax.inject.{Inject, Singleton}

import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.agentepayeregistration.models.RegistrationDetails
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
    Seq(Index(key = Seq("id" -> IndexType.Ascending), name = Some("id"), unique = true))

  def create(registrationDetails: RegistrationDetails)(implicit ec: ExecutionContext): Future[Int] =
    insert(registrationDetails).map(_.n)
}
