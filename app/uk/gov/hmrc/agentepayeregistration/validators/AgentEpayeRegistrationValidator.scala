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

package uk.gov.hmrc.agentepayeregistration.validators

import utils.EmailAddressValidation
import cats.Semigroup
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import uk.gov.hmrc.agentepayeregistration.models.{Failure, RegistrationRequest}

import java.time.{LocalDate, ZoneOffset}
import java.time.temporal.ChronoUnit

object ValidatedSemigroup {

  implicit def validatedSemigroup[A]: Semigroup[Validated[Failure, Unit]] = new Semigroup[Validated[Failure, Unit]] {
    def combine(x: Validated[Failure, Unit], y: Validated[Failure, Unit]): Validated[Failure, Unit] = (x, y) match {
      case (Valid(_), Valid(_))       => Valid(())
      case (Invalid(f1), Invalid(f2)) => Invalid(Failure(f1.errors ++ f2.errors))
      case (Valid(_), f @ Invalid(_)) => f
      case (f @ Invalid(_), Valid(_)) => f
    }
  }

}

object AgentEpayeRegistrationValidator {
  import ValidatedSemigroup._

  def validateRegistrationRequest(request: RegistrationRequest): Validated[Failure, Unit] = {

    def mandatoryChars(field: String, propertyName: String, limit: Int) =
      nonEmpty(field)(propertyName)
        .andThen(_ => isValidCharacters(field)(propertyName))
        .andThen(_ => maxLength(field, limit)(propertyName))

    def mandatoryPostcode(field: String, propertyName: String) =
      nonEmpty(field)(propertyName)
        .andThen(_ => isPostcode(field)(propertyName))
        .andThen(_ => maxLength(field, 8)(propertyName))

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

    validate(validators ++ optionalFieldValidators)
  }

  def validateDateRange(dateFrom: LocalDate, dateTo: LocalDate): Validated[Failure, Unit] =
    validate(
      Seq(
        isInPast(dateFrom)("From")
          .andThen(_ => isInPast(dateTo)("To"))
          .andThen(_ => isValidDateRange(dateFrom, dateTo))
      )
    )

  private def validate(validators: Seq[Validated[Failure, Unit]]): Validated[Failure, Unit] =
    Semigroup[Validated[Failure, Unit]]
      .combineAllOption(validators)
      .getOrElse(Valid(()))

  private[validators] def nonEmpty(field: String)(propertyName: String) =
    if (field.trim.nonEmpty)
      Valid(())
    else
      Invalid(Failure("MISSING_FIELD", s"The $propertyName field is mandatory"))

  private[validators] def isValidCharacters(field: String)(propertyName: String) =
    if (field.matches("[A-Za-z0-9,.()\\!@\\- ]+"))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field contains invalid characters"))

  private[validators] def maxLength(field: String, maxLength: Int)(propertyName: String) =
    if (field.trim.length <= maxLength)
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field exceeds $maxLength characters"))

  private[validators] def isEmailAddress(field: String)(propertyName: String) =
    if (new EmailAddressValidation().isValid(field))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field is not a valid email"))

  private[validators] def isPostcode(field: String)(propertyName: String) = {
    val postcodeRegex = "^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}$|BFPO\\s?[0-9]{1,3}$".r

    if (postcodeRegex.matches(field))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field is not a valid postcode"))
  }

  private[validators] def isPhoneNumber(field: String)(propertyName: String) =
    if (field.matches("^[0-9 ]*$"))
      Valid(())
    else
      Invalid(Failure("INVALID_FIELD", s"The $propertyName field is not a valid phone number"))

  private[validators] def isInPast(date: LocalDate)(paramName: String) = {
    val today = LocalDate.now(ZoneOffset.UTC)
    if (today.isAfter(date))
      Valid(())
    else
      Invalid(Failure("INVALID_DATE_RANGE", s"'$paramName' date must be in the past"))
  }

  private[validators] def isValidDateRange(from: LocalDate, to: LocalDate) =
    if (from.isAfter(to))
      Invalid(Failure("INVALID_DATE_RANGE", "'To' date must be after 'From' date"))
    else if (!to.equals(from.plusYears(1)) && from.until(to, ChronoUnit.DAYS) > 365)
      Invalid(Failure("INVALID_DATE_RANGE", "Date range must be 1 year or less"))
    else
      Valid(())

}
