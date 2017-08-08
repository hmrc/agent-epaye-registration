package uk.gov.hmrc.agentepayeregistration.services

import javax.inject.{Inject, Singleton}

import cats.data.Validated.{Invalid, Valid}
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, Failure, RegistrationRequest}
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.agentepayeregistration.validators.AgentEpayeRegistrationValidator._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentEpayeRegistrationService @Inject()(repository: AgentEpayeRegistrationRepository) {

  def register(request: RegistrationRequest)(implicit ec: ExecutionContext): Future[Either[Failure, AgentReference]] = {
    validate(request) match {
      case Valid(_) => repository.create(request).map(Right(_))
      case Invalid(failure) => Future.successful(Left(failure))
    }
  }
}
