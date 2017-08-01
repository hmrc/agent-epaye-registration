package uk.gov.hmrc.agentepayeregistration.controllers

import javax.inject._

import play.api.mvc.{Action, AnyContent, Controller}

@Singleton
class AgentEpayeRegistrationController @Inject() extends Controller {

	def register(): Action[AnyContent] = Action {
	Ok
}
}
