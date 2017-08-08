package uk.gov.hmrc.agentepayeregistration.models

import play.api.libs.json.Json

case class Address(addressLine1: String,
                   addressLine2: String,
                   addressLine3: Option[String],
                   addressLine4: Option[String],
                   postCode: String)

object Address {
  implicit val addressFormat = Json.format[Address]
}