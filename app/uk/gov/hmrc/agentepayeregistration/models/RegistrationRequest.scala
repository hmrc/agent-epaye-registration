package uk.gov.hmrc.agentepayeregistration.models

import play.api.libs.json.Json

case class RegistrationRequest(agentName: String,
                               contactName: String,
                               telephoneNumber: Option[String],
                               faxNumber: Option[String],
                               emailAddress: Option[String],
                               address: Address)

object RegistrationRequest {
  implicit val registrationRequestFormat = Json.format[RegistrationRequest]
}
