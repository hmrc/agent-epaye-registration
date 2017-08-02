package uk.gov.hmrc.agentepayeregistration.controllers

import javax.inject._

import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.agentepayeregistration.models.RegistrationDetails

@Singleton
class AgentEpayeRegistrationController @Inject() extends Controller {

	val register: Action[AnyContent] = Action.async(parse.json) { implicit request =>
		request.body.validate[RegistrationDetails].map { details =>
      

    }

	Ok
}
}
