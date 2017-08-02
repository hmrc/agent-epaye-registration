package uk.gov.hmrc.agentepayeregistration.services

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.agentepayeregistration.models.RegistrationDetails
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AgentEpayeRegistrationService @Inject()(repository: AgentEpayeRegistrationRepository) {

  def register(details: RegistrationDetails): Future[Unit] =
    repository.create(details).map(_ => ())

}
