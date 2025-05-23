/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}

case class CreateKnownFactsRequest(
    agentName: Option[String],
    contactName: Option[String],
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    postCode: String,
    phoneNo: Option[String],
    faxNumber: Option[String],
    email: Option[String],
    createdDate: String
)

object CreateKnownFactsRequest {
  implicit val createKnownFactsRequestFormat: OFormat[CreateKnownFactsRequest] = Json.format[CreateKnownFactsRequest]

  def apply(regRequest: RegistrationRequest, createdDate: String): CreateKnownFactsRequest =
    CreateKnownFactsRequest(
      Some(regRequest.agentName),
      Some(regRequest.contactName),
      regRequest.address.addressLine1,
      regRequest.address.addressLine2,
      regRequest.address.addressLine3,
      regRequest.address.addressLine4,
      regRequest.address.postCode,
      regRequest.telephoneNumber,
      regRequest.faxNumber,
      regRequest.emailAddress,
      createdDate
    )

}
