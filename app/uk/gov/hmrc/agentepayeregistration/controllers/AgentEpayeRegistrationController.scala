package uk.gov.hmrc.agentepayeregistration.controllers

import javax.inject._

import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.agentepayeregistration.models.RegistrationDetails
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class AgentEpayeRegistrationController @Inject()(service: AgentEpayeRegistrationService) extends Controller {

  val register = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationDetails].map { details =>
      service.register(details).map(_ => Ok)
    }.recoverTotal(_ => Future.successful(BadRequest))
  }
}
