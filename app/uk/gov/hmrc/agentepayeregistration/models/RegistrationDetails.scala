package uk.gov.hmrc.agentepayeregistration.models

import play.api.libs.json.Json

case class RegistrationDetails(agentName: String,
                               contactName: String,
                               telephoneNumber: String,
                               faxNumber: String,
                               emailAddress: String,
                               address: Address)

object RegistrationDetails {
  implicit val registrationDetailsFormat = Json.format[RegistrationDetails]
}
