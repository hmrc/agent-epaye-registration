/*
 * Copyright 2019 HM Revenue & Customs
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

import org.joda.time.DateTimeZone
import play.api.libs.json.Json

case class RegistrationExtraction(agentReference: String,
                                  agentName: String,
                                  contactName: String,
                                  telephoneNumber: Option[String],
                                  faxNumber: Option[String],
                                  emailAddress: Option[String],
                                  addressLine1: String,
                                  addressLine2: String,
                                  addressLine3: Option[String],
                                  addressLine4: Option[String],
                                  postCode: String,
                                  createdDateTime: String)

object RegistrationExtraction {
  def apply(details: RegistrationDetails): RegistrationExtraction = {
    import details._
    import details.registration._
    import details.registration.address._
    RegistrationExtraction(agentReference.value,
      agentName,
      contactName,
      telephoneNumber,
      faxNumber,
      emailAddress,
      addressLine1,
      addressLine2,
      addressLine3,
      addressLine4,
      postCode,
      createdDateTime.toDateTime(DateTimeZone.UTC).toString)
  }

  implicit val registrationExtractionWrites = Json.writes[RegistrationExtraction]
}
