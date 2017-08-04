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

    def mandatoryChars(field: String, propertyName: String) =
      nonEmpty(field)(propertyName).andThen(_ => isValidCharacters(field)(propertyName))

    val validators = Seq(
      mandatoryChars(request.agentName, "agent name"),
      mandatoryChars(request.contactName, "contact name"),
      mandatoryChars(request.address.addressLine1, "address line 1"),
      mandatoryChars(request.address.addressLine2, "address line 2"),
      mandatoryChars(request.address.postCode, "postcode")
    )

    val optionalFieldValidators = Seq(
      request.telephoneNumber.map(isInteger(_)("telephone number")),
      request.faxNumber.map(isInteger(_)(("fax number"))),
      request.emailAddress.map(isValidCharacters(_)("email address")),
      request.address.addressLine3.map(isValidCharacters(_)("address line 3")),
      request.address.addressLine4.map(isValidCharacters(_)("address line 4"))
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

  private def isInteger(field: String)(propertyName: String) =
    if (Try(field.toInt).isSuccess)
      Valid(())
    else
      Invalid(Failure(Set(ValidationError("INVALID_FIELD", s"The $propertyName field is not an integer"))))
}

