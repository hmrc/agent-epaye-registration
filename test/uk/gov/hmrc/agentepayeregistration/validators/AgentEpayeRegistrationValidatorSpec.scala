package uk.gov.hmrc.agentepayeregistration.validators

import cats.data.Validated.{Invalid, Valid}
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentepayeregistration.controllers.BaseControllerISpec
import uk.gov.hmrc.agentepayeregistration.models.{Address, Failure, RegistrationRequest, ValidationError}

class AgentEpayeRegistrationValidatorSpec extends BaseControllerISpec {
  private lazy val validator = app.injector.instanceOf[AgentEpayeRegistrationValidator]

  private val address = Address("29 Acacia Road", "Nuttytown", Some("Bannastate"), Some("Country"), "AB11 AA1")
  private val regRequest = RegistrationRequest("Dave Agent",
    "John Contact",
    Some("0123456789"),
    Some("0123456780"),
    Some("email@test.com"),
    address)

  private def anError(errorCode: String, errorMsg: String) = Invalid(Failure(Set(ValidationError(errorCode, errorMsg))))


  "validate should pass" when {
    "all values are valid" in {
      validator.validate(regRequest) shouldBe Valid()
    }

    "the optional telephone number is not given" in {
      validator.validate(regRequest.copy(telephoneNumber = None)) shouldBe Valid()
    }
    "the optional fax number is not given" in {
      validator.validate(regRequest.copy(faxNumber = None)) shouldBe Valid()
    }
    "the optional third address line is not given" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine3 = None))) shouldBe Valid()
    }
    "the optional fourth address line is not given" in {
      validator.validate(regRequest.copy(address = address.copy(addressLine4 = None))) shouldBe Valid()
    }

    "the email address is unusual but valid" in {
      validator.validate(regRequest.copy(emailAddress = Some("a+b@c.com"))) shouldBe Valid()
      validator.validate(regRequest.copy(emailAddress = Some("a_b@c.com"))) shouldBe Valid()
      validator.validate(regRequest.copy(emailAddress = Some("a.b@c.com"))) shouldBe Valid()
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
        anError("INVALID_FIELD", "The postcode field contains invalid characters")
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
        anError("INVALID_FIELD", "The email address field contains invalid characters")
    }
  }
}
