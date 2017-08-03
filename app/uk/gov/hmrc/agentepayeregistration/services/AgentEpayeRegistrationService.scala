package uk.gov.hmrc.agentepayeregistration.services

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, RegistrationRequest}
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationService @Inject()(repository: AgentEpayeRegistrationRepository) {

  def register(details: RegistrationRequest)(implicit ec: ExecutionContext): Future[AgentReference] =
    repository.create(details)

}
