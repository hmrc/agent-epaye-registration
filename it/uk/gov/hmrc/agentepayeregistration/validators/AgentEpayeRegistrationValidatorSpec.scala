package uk.gov.hmrc.agentepayeregistration.validators

import cats.data.Validated.{Invalid, Valid}
import uk.gov.hmrc.agentepayeregistration.controllers.BaseControllerISpec
import uk.gov.hmrc.agentepayeregistration.models.{Address, Failure, RegistrationRequest, ValidationError}

class AgentEpayeRegistrationValidatorSpec extends BaseControllerISpec {
  private lazy val validator = app.injector.instanceOf[AgentEpayeRegistrationValidator]

  private val address = Address("29 Acacia Road", "Nuttytown", Some("Bannastate"), Some("Country"), "AA11AA")
  private val regRequest = RegistrationRequest("Dave Agent",
    "John Contact",
    Some("0123456789"),
    Some("0123456780"),
    Some("email@test.com"),
    address)

  private def anError(errorCode: String, errorMsg: String) = Invalid(Failure(Set(ValidationError(errorCode, errorMsg))))

  private def padField(chars: Int) = Seq.fill(chars)('1').mkString

  "validate should pass" when {
    "all values are valid" in {
      validator.validate(regRequest) shouldBe Valid(())
    }

    "the optional telephone number is not given" in {
      validator.validate(regRequest.copy(telephoneNumber = None)) shouldBe Valid(())
    }
    "the optional fax number is not given" in {
      validator.validate(regRequest.copy(faxNumber = None)) shouldBe Valid(())
    }
    "the optional third address line is not given" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine3 = None))) shouldBe Valid(())
    }
    "the optional fourth address line is not given" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine4 = None))) shouldBe Valid(())
    }

    "the email address is unusual but valid" in {
      validator.validate(regRequest.copy(emailAddress = Some("a+b@c.com"))) shouldBe Valid(())
      validator.validate(regRequest.copy(emailAddress = Some("a_b@c.com"))) shouldBe Valid(())
      validator.validate(regRequest.copy(emailAddress = Some("a.b@c.com"))) shouldBe Valid(())
    }
  }


  "validate should not pass" when {
    "an agent name is not given" in {
      validator.validate(regRequest.copy(agentName = " ")) shouldBe
        anError("MISSING_FIELD", "The agent name field is mandatory")
    }
    "the agent name contains invalid characters" in {
      validator.validate(regRequest.copy(agentName = "~")) shouldBe
        anError("INVALID_FIELD", "The agent name field contains invalid characters")
    }

    "a contact name is not given" in {
      validator.validate(regRequest.copy(contactName = " ")) shouldBe
        anError("MISSING_FIELD", "The contact name field is mandatory")
    }
    "a contact name contains invalid characters" in {
      validator.validate(regRequest.copy(contactName = "~")) shouldBe
        anError("INVALID_FIELD", "The contact name field contains invalid characters")
    }

    "the first address line is not given" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine1 = " "))) shouldBe
        anError("MISSING_FIELD", "The address line 1 field is mandatory")
    }
    "the first address contains invalid characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine1 = "~"))) shouldBe
        anError("INVALID_FIELD", "The address line 1 field contains invalid characters")
    }

    "the second address line is not given" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine2 = " "))) shouldBe
        anError("MISSING_FIELD", "The address line 2 field is mandatory")
    }
    "the second address contains invalid characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine2 = "~"))) shouldBe
        anError("INVALID_FIELD", "The address line 2 field contains invalid characters")
    }

    "the third address contains invalid characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine3 = Some("~")))) shouldBe
        anError("INVALID_FIELD", "The address line 3 field contains invalid characters")
    }
    "the fourth address contains invalid characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine4 = Some("~")))) shouldBe
        anError("INVALID_FIELD", "The address line 4 field contains invalid characters")
    }

    "the postcode is not given" in {
      validator.validate(regRequest.copy(address = address.copy(postCode = " "))) shouldBe
        anError("MISSING_FIELD", "The postcode field is mandatory")
    }
    "the postcode contains invalid characters" in {
      validator.validate(regRequest.copy(address = address.copy(postCode = "~"))) shouldBe
        anError("INVALID_FIELD", "The postcode field is not a valid postcode")
    }

    "the telephone number is not numeric" in {
      validator.validate(regRequest.copy(telephoneNumber = Some("abc"))) shouldBe
        anError("INVALID_FIELD", "The telephone number field is not an integer")
    }

    "the fax number is not numeric" in {
      validator.validate(regRequest.copy(faxNumber = Some("abc"))) shouldBe
        anError("INVALID_FIELD", "The fax number field is not an integer")
    }

    "the email address contains invalid characters" in {
      validator.validate(regRequest.copy(emailAddress = Some("`"))) shouldBe
        anError("INVALID_FIELD", "The email address field is not a valid email")
    }

    "if multiple fields are invalid, all the validation errors should be reported" in {
      validator.validate(regRequest.copy(agentName = " ", contactName = " ")) shouldBe
        Invalid(Failure(Set(
          ValidationError("MISSING_FIELD", "The agent name field is mandatory"),
          ValidationError("MISSING_FIELD", "The contact name field is mandatory")
        )))
    }

    "the agent name is longer than 56 characters" in {
      validator.validate(regRequest.copy(agentName = padField(57))) shouldBe
        anError("INVALID_FIELD", "The agent name field exceeds 56 characters")
    }
    "the contact name is longer than 56 characters" in {
      validator.validate(regRequest.copy(contactName = padField(57))) shouldBe
        anError("INVALID_FIELD", "The contact name field exceeds 56 characters")
    }
    "the telephone number is longer than 35 characters" in {
      validator.validate(regRequest.copy(telephoneNumber = Some(padField(36)))) shouldBe
        anError("INVALID_FIELD", "The telephone number field exceeds 35 characters")
    }
    "the fax number is longer than 35 characters" in {
      validator.validate(regRequest.copy(faxNumber = Some(padField(36)))) shouldBe
        anError("INVALID_FIELD", "The fax number field exceeds 35 characters")

    }
    "the email address is longer than 129 characters" in {
      validator.validate(regRequest.copy(emailAddress = Some(s"${padField(132)}@example.org"))) shouldBe
        anError("INVALID_FIELD", "The email address field exceeds 129 characters")
    }
    "the address line 1 is longer than 35 characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine1 = padField(36)))) shouldBe
        anError("INVALID_FIELD", "The address line 1 field exceeds 35 characters")
    }
    "the address line 2 is longer than 35 characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine2 = padField(36)))) shouldBe
        anError("INVALID_FIELD", "The address line 2 field exceeds 35 characters")
    }
    "the address line 3 is longer than 35 characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine3 = Some(padField(36))))) shouldBe
        anError("INVALID_FIELD", "The address line 3 field exceeds 35 characters")
    }
    "the address line 4 is longer than 35 characters" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine4 = Some(padField(36))))) shouldBe
        anError("INVALID_FIELD", "The address line 4 field exceeds 35 characters")
    }
  }
}
