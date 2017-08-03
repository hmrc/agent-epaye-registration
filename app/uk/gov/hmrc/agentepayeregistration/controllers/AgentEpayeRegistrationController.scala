package uk.gov.hmrc.agentepayeregistration.controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.agentepayeregistration.models.RegistrationRequest
import uk.gov.hmrc.agentepayeregistration.services.AgentEpayeRegistrationService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

@Singleton
class AgentEpayeRegistrationController @Inject()(service: AgentEpayeRegistrationService) extends BaseController {

  val register = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationRequest].map { details =>
      service.register(details).map(x => Ok(Json.toJson(x)))
    }.recoverTotal(_ => Future.successful(BadRequest))
  }
}
