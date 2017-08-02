package uk.gov.hmrc.agentepayeregistration.services

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.agentepayeregistration.models.RegistrationDetails
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationService @Inject()(repository: AgentEpayeRegistrationRepository) {

  def register(details: RegistrationDetails)(implicit ec: ExecutionContext): Future[Unit] =
    repository.create(details).map(_ => ())

}
