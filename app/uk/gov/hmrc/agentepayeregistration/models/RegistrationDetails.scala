package uk.gov.hmrc.agentepayeregistration.models

import play.api.libs.json.Json

case class RegistrationDetails(agentName: String,
                               contactName: String,
                               telephoneNumber: Option[String],
                               faxNumber: Option[String],
                               emailAddress: Option[String],
                               address: Address)

object RegistrationDetails {
  implicit val registrationDetailsFormat = Json.format[RegistrationDetails]
}
