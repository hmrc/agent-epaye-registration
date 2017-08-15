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

package uk.gov.hmrc.agentepayeregistration.validators

import cats.Semigroup
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import uk.gov.hmrc.agentepayeregistration.models.{Failure, RegistrationRequest}

object ValidatedSemigroup {
  implicit def validatedSemigroup[A] = new Semigroup[Validated[Failure, Unit]] {
    def combine(x: Validated[Failure, Unit], y: Validated[Failure, Unit]): Validated[Failure, Unit] = (x, y) match {
      case (Valid(_), Valid(_)) => Valid(())
      case (Invalid(f1),Invalid(f2)) => Invalid(Failure(f1.errors ++ f2.errors))
      case (Valid(_), f@Invalid(_)) => f
      case (f@Invalid(_), Valid(_)) => f
    }
  }
}

object AgentEpayeRegistrationValidator {
  import ValidatedSemigroup._

  def validate(request: RegistrationRequest): Validated[Failure, Unit] = {

    def mandatoryChars(field: String, propertyName: String, limit: Int) =
      nonEmpty(field)(propertyName)
        .andThen(_ => isValidCharacters(field)(propertyName))
        .andThen(_ => maxLength(field, limit)(propertyName))

    def mandatoryPostcode(field: String, propertyName: String) =
      nonEmpty(field)("postcode").andThen(_ => isPostcode(field)("postcode"))

    val validators = Seq(
      mandatoryChars(request.agentName, "agent name", 56),
      mandatoryChars(request.contactName, "contact name", 56),
      mandatoryChars(request.address.addressLine1, "address line 1", 35),
      mandatoryChars(request.address.addressLine2, "address line 2", 35),
      mandatoryPostcode(request.address.postCode, "postcode")
    )

    def validCharsWithLimit(field: String, propertyName: String, limit: Int) =
      isValidCharacters(field)(propertyName).andThen(_ => maxLength(field, limit)(propertyName))

    def emailValidatorWithLimit(field: String, propertyName: String, limit: Int) =
      isEmailAddress(field)(propertyName).andThen(_ => maxLength(field, limit)(propertyName))

    def phoneValidatorWithLimit(field: String, propertyName: String, limit: Int) =
      isPhoneNumber(field)(propertyName).andThen(_ => maxLength(field, limit)(propertyName))


    val optionalFieldValidators = Seq(
      request.telephoneNumber.map(x => phoneValidatorWithLimit(x, "telephone number", 35)),
      request.faxNumber.map(x => phoneValidatorWithLimit(x, "fax number", 35)),
      request.emailAddress.map(x => emailValidatorWithLimit(x, "email address", 129)),
      request.address.addressLine3.map(x => validCharsWithLimit(x, "address line 3", 35)),
      request.address.addressLine4.map(x => validCharsWithLimit(x, "address line 4", 35))
    ).flatten

    Semigroup[Validated[Failure, Unit]]
      .combineAllOption(validators ++ optionalFieldValidators)
      .getOrElse(Valid(()))
  }

  private[validators] def nonEmpty(field: String)(propertyName: String) =
    if (field.trim.nonEmpty)
      Valid(())
    else
      Invalid(Failure("MISSING_FIELD", s"The $propertyName field is mandatory"))

  private[validators] def isValidCharacters(field: String)(propertyName: String) =
    if (field.matches("[a-zA-Z0-9,.()\\-\\!@\\s]+"))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field contains invalid characters"))

  private[validators] def maxLength(field: String, maxLength: Int)(propertyName: String) =
    if (field.trim.length <= maxLength)
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field exceeds $maxLength characters"))

  private[validators] def isEmailAddress(field: String)(propertyName: String) =
    if (field.matches("""(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$)"""))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field is not a valid email"))

  private[validators] def isPostcode(field: String)(propertyName: String) =
    if (field.matches("^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}$|BFPO\\s?[0-9]{1,5}$"))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field is not a valid postcode"))

  private[validators] def isPhoneNumber(field: String)(propertyName: String) =
    if (field.matches("^[0-9- +()#x ]*$"))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field is not a valid phone number"))
}
