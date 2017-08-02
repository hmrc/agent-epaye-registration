package uk.gov.hmrc.agentepayeregistration.controllers

import javax.inject._

import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.agentepayeregistration.models.RegistrationDetails
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService
import uk.gov.hmrc.play.microservice.controller.BaseController

import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import scala.concurrent.Future


@Singleton
class AgentEpayeRegistrationController @Inject()(service: AgentEpayeRegistrationService) extends BaseController {

  val register = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationDetails].map { details =>
      service.register(details).map(_ => Ok)
    }.recoverTotal(_ => Future.successful(BadRequest))
  }
}
