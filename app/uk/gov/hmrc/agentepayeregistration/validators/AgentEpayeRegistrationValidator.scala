package uk.gov.hmrc.agentepayeregistration.validators

import javax.inject.{Inject, Singleton}

import cats.Semigroup
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import uk.gov.hmrc.agentepayeregistration.models.{RegistrationRequest, Failure, ValidationError}

import scala.util.Try

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

@Singleton
class AgentEpayeRegistrationValidator @Inject()() {

  import ValidatedSemigroup._

  def validate(request: RegistrationRequest): Validated[Failure, Unit] = {

    def mandatoryChars(field: String, propertyName: String, limit: Int) =
      nonEmpty(field)(propertyName)
        .andThen(_ => isValidCharacters(field)(propertyName))
        .andThen(_ => maxLength(field, limit)(propertyName))

    val validators = Seq(
      mandatoryChars(request.agentName, "agent name", 56),
      mandatoryChars(request.contactName, "contact name", 56),
      mandatoryChars(request.address.addressLine1, "address line 1", 35),
      mandatoryChars(request.address.addressLine2, "address line 2", 35),
      mandatoryChars(request.address.postCode, "postcode", 8)
    )

    def numberWithLimit(field: String, propertyName: String, limit: Int) =
      isInteger(field)(propertyName).andThen(_ => maxLength(field, limit)(propertyName))

    def validCharsWithLimit(field: String, propertyName: String, limit: Int) =
      isValidCharacters(field)(propertyName).andThen(_ => maxLength(field, limit)(propertyName))

    val optionalFieldValidators = Seq(
      request.telephoneNumber.map(x => numberWithLimit(x, "telephone number", 35)),
      request.faxNumber.map(x => numberWithLimit(x, "fax number", 35)),
      request.emailAddress.map(x => validCharsWithLimit(x, "email address", 129)),
      request.address.addressLine3.map(x => validCharsWithLimit(x, "address line 3", 35)),
      request.address.addressLine4.map(x => validCharsWithLimit(x, "address line 4", 35))
    ).flatten

    Semigroup[Validated[Failure, Unit]]
      .combineAllOption(validators ++ optionalFieldValidators)
      .getOrElse(Valid(()))
  }

  private def nonEmpty(field: String)(propertyName: String) =
    if (field.trim.nonEmpty)
      Valid(())
    else
      Invalid(Failure(Set(ValidationError("MISSING_FIELD", s"The $propertyName field is mandatory"))))

  private def isValidCharacters(field: String)(propertyName: String) =
    if (field.matches("[a-zA-Z0-9,.()\\-\\!@\\s]+"))
      Valid(())
    else
      Invalid(Failure(Set(ValidationError("INVALID_FIELD", s"The $propertyName field contains invalid characters"))))

  private def maxLength(field: String, maxLength: Int)(propertyName: String) =
    if (field.trim.length <= maxLength)
      Valid(())
    else
      Invalid(Failure(Set(ValidationError("INVALID_FIELD", s"The $propertyName field exceeds $maxLength characters"))))

  private def isInteger(field: String)(propertyName: String) =
    if (field.matches("[0-9]+"))
      Valid(())
    else
      Invalid(Failure(Set(ValidationError("INVALID_FIELD", s"The $propertyName field is not an integer"))))
}

