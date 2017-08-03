package uk.gov.hmrc.agentepayeregistration.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

case class RegistrationDetails(agentReference: AgentReference,
                               registration: RegistrationRequest)

object RegistrationDetails {

  implicit val registrationDetailsFormat: Format[RegistrationDetails] = (
    (JsPath \ "agentReference").format[String] and
      (JsPath \ "agentName").format[String] and
      (JsPath \ "contactName").format[String] and
      (JsPath \ "telephoneNumber").formatNullable[String] and
      (JsPath \ "faxNumber").formatNullable[String] and
      (JsPath \ "emailAddress").formatNullable[String] and
      (JsPath \ "address").format[Address]
    ) ((agentRef, agentName, contactName, telNo, faxNo, emailAddr, address) =>
    RegistrationDetails(AgentReference(agentRef),
      RegistrationRequest(agentName, contactName, telNo, faxNo, emailAddr, address)
    ), (details => (
      details.agentReference.value,
      details.registration.agentName,
      details.registration.contactName,
      details.registration.telephoneNumber,
      details.registration.faxNumber,
      details.registration.emailAddress,
      details.registration.address
    )))

}
