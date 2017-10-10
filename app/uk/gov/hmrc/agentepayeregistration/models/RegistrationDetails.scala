/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentepayeregistration.models

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class RegistrationDetails(agentReference: AgentReference,
                               registration: RegistrationRequest,
                               createdDateTime: DateTime)

object RegistrationDetails {

  implicit val registrationDetailsFormat: OFormat[RegistrationDetails] = (
    (JsPath \ "agentReference").format[String] and
      (JsPath \ "agentName").format[String] and
      (JsPath \ "contactName").format[String] and
      (JsPath \ "telephoneNumber").formatNullable[String] and
      (JsPath \ "faxNumber").formatNullable[String] and
      (JsPath \ "emailAddress").formatNullable[String] and
      (JsPath \ "address").format[Address] and
      (JsPath \ "createdDateTime").format[DateTime](ReactiveMongoFormats.dateTimeFormats)
    ) ((agentRef, agentName, contactName, telNo, faxNo, emailAddr, address, createdDateTime) =>
    RegistrationDetails(AgentReference(agentRef),
      RegistrationRequest(agentName, contactName, telNo, faxNo, emailAddr, address),
      createdDateTime
    ), details => (
    details.agentReference.value,
    details.registration.agentName,
    details.registration.contactName,
    details.registration.telephoneNumber,
    details.registration.faxNumber,
    details.registration.emailAddress,
    details.registration.address,
    details.createdDateTime
  ))

}
