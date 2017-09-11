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

import cats.data.Validated.{Invalid, Valid}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTimeZone, Days, LocalDate}
import uk.gov.hmrc.agentepayeregistration.models.{Address, Failure, RegistrationRequest, ValidationError}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.agentepayeregistration.validators.AgentEpayeRegistrationValidator._

class AgentEpayeRegistrationValidatorSpec extends UnitSpec {
  private val address = Address("29 Acacia Road", "Nuttytown", Some("Bannastate"), Some("Country"), "AA11AA")
  private val regRequest = RegistrationRequest("Dave Agent",
    "John Contact",
    Some("0123456789"),
    Some("0123456780"),
    Some("email@test.com"),
    address)

  private def anError(errorCode: String, errorMsg: String) = Invalid(Failure(Set(ValidationError(errorCode, errorMsg))))

  private def padField(chars: Int) = Seq.fill(chars)('1').mkString

  "nonEmpty validator" should {
    "not allow just whitespace" in {
      val expected = Invalid(Failure("MISSING_FIELD", s"The sample field is mandatory"))
      AgentEpayeRegistrationValidator.nonEmpty("")("sample") shouldBe expected
      AgentEpayeRegistrationValidator.nonEmpty(" ")("sample") shouldBe expected
      AgentEpayeRegistrationValidator.nonEmpty("\t")("sample") shouldBe expected
      AgentEpayeRegistrationValidator.nonEmpty("   \n")("sample") shouldBe expected
    }

    "allow with non-whitespace" in {
      AgentEpayeRegistrationValidator.nonEmpty("s")("sample") shouldBe Valid(())
      AgentEpayeRegistrationValidator.nonEmpty(" s")("sample") shouldBe Valid(())
      AgentEpayeRegistrationValidator.nonEmpty("s ")("sample") shouldBe Valid(())
      AgentEpayeRegistrationValidator.nonEmpty("s\ts\n")("sample") shouldBe Valid(())
    }
  }

  "isValidCharacters validator" should {
    "disallow invalid characters" in {
      val expectedInvalid = Invalid(Failure("INVALID_FIELD", s"The sample field contains invalid characters"))
      AgentEpayeRegistrationValidator.isValidCharacters("~")("sample") shouldBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("[")("sample") shouldBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("#")("sample") shouldBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("(")("sample") shouldBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters(")")("sample") shouldBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("@")("sample") shouldBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("!")("sample") shouldBe expectedInvalid
    }

    "allow a-z" in {
      AgentEpayeRegistrationValidator.nonEmpty("abcdefghijklmnopqrstuvwxyz")("sample") shouldBe Valid(())
    }
    "allow A-Z" in {
      AgentEpayeRegistrationValidator.nonEmpty("ABCDEFGHIJKLMNOPQRSTUVWXYZ")("sample") shouldBe Valid(())
    }
    "allow 0-9" in {
      AgentEpayeRegistrationValidator.nonEmpty("01234567890")("sample") shouldBe Valid(())
    }
    "allow commas, periods, -, \\, /, ', &, and space" in {
      AgentEpayeRegistrationValidator.nonEmpty(",.-\\/'& ")("sample") shouldBe Valid(())
    }
  }

  "maxLength validator" should {
    "pass if the length is less than the maximum" in {
      AgentEpayeRegistrationValidator.maxLength("1", 2)("x") shouldBe Valid(())
    }
    "pass if the length is equal to the maximum" in {
      AgentEpayeRegistrationValidator.maxLength("22", 2)("x") shouldBe Valid(())
    }
    "fail if the length is greater than the maximum" in {
      AgentEpayeRegistrationValidator.maxLength("333", 2)("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field exceeds 2 characters"))
    }
  }

  "isEmailAddress validator" should {
    "allow a simple email address" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a@b.com")("x") shouldBe Valid(())
    }

    "allow email addresses with a hyphen, plus, period, underscore, exclamations, or numbers" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a-b@b.com")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a+b@b.com")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a.b@b.com")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a_b@b.com")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a!b@b.com")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("1@b.com")("x") shouldBe Valid(())
    }

    "not allow an email address without an @ symbol" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a.com")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }

    "not allow an email address with more than one @ symbol" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a@b@c.com")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }

    "not allow an email address without a domain" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a@")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }

    "not allow an email address without a local-part" in {
      AgentEpayeRegistrationValidator.isEmailAddress("@b.com")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }
  }

  "isPostcode validator" should {
    "pass the 6 main formats of postcodes" in {
      AgentEpayeRegistrationValidator.isPostcode("AA9A9AA")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("A9A9AA")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("A99AA")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("A999AA")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("AA99AA")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("AA999AA")("x") shouldBe Valid(())
    }

    "pass BFPO codes" in {
      AgentEpayeRegistrationValidator.isPostcode("BFPO 1")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("BFPO1234")("x") shouldBe Valid(())
    }

    "fail a valid postcode with lowercase characters" in {
      AgentEpayeRegistrationValidator.isPostcode("aa999aa")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid postcode"))
    }

    "fail any code that isn't one of the 6 main formats of postcodes" in {
      AgentEpayeRegistrationValidator.isPostcode("A99999A")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid postcode"))
    }

    "fail a code that is longer than 8 characters" in {
      AgentEpayeRegistrationValidator.isPostcode("AA9A9AAAA")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid postcode"))
    }
  }

  "isPhoneNumber validator" should {
    "pass just numbers" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("01234567890")("x") shouldBe Valid(())
    }
    "pass numbers separated by a space" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("01234 567890")("x") shouldBe Valid(())
    }
    "pass numbers with hash" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("#01234 567890")("x") shouldBe Valid(())
    }
    "pass numbers with an x" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("01234 567890x2")("x") shouldBe Valid(())
    }
    "pass with a country code prefix" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("+441234 567890")("x") shouldBe Valid(())
      AgentEpayeRegistrationValidator.isPhoneNumber("00441234 567890")("x") shouldBe Valid(())
    }
    "pass with bracketed area code" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("+44(0)1234 567890")("x") shouldBe Valid(())
    }
    "pass with hyphens" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("44-1234-567890")("x") shouldBe Valid(())
    }
    "fail with letters" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("44-1234-ACME12")("x") shouldBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid phone number"))
    }
  }

  "isInPast" should {
    "pass a date in the past" in {
      val dateYesterday = LocalDate.now(DateTimeZone.UTC).minusDays(1)
      AgentEpayeRegistrationValidator.isInPast(dateYesterday)("x") shouldBe Valid(())
    }
    "fail a date in the present" in {
      val dateToday = LocalDate.now(DateTimeZone.UTC)
      AgentEpayeRegistrationValidator.isInPast(dateToday)("x") shouldBe
        Invalid(Failure("INVALID_DATE_RANGE", "'x' date must be in the past"))
    }
    "fail a date in the future" in {
      val dateTomorrow = LocalDate.now(DateTimeZone.UTC).plusDays(1)
      AgentEpayeRegistrationValidator.isInPast(dateTomorrow)("x") shouldBe
        Invalid(Failure("INVALID_DATE_RANGE", "'x' date must be in the past"))
    }
  }

  "isValidDateRange" should {
    "pass a valid date range" in {
      val dateYesterday = LocalDate.now(DateTimeZone.UTC).minusDays(1)
      AgentEpayeRegistrationValidator.isValidDateRange(dateYesterday, dateYesterday) shouldBe Valid(())
      AgentEpayeRegistrationValidator.isValidDateRange(dateYesterday.minusDays(1), dateYesterday) shouldBe Valid(())
    }
    "fail if the from date occurs after the to date" in {
      val dateYesterday = LocalDate.now(DateTimeZone.UTC).minusDays(1)
      AgentEpayeRegistrationValidator.isValidDateRange(dateYesterday, dateYesterday.minusDays(1)) shouldBe
        anError("INVALID_DATE_RANGE", "'To' date must be after 'From' date")
    }
    "fail if the date range spans more than a year" in {
      val someYear = LocalDate.parse("2015-03-01", ISODateTimeFormat.date())
      AgentEpayeRegistrationValidator.isValidDateRange(someYear, someYear.plusYears(1).plusDays(1)) shouldBe
        anError("INVALID_DATE_RANGE", "Date range must be 1 year or less")
    }
    "pass if the date range spans exactly 366 days of a leap year" in {
      val yearBeforeFeb29 = LocalDate.parse("2015-03-01", ISODateTimeFormat.date())
      val yearAfterFeb29 = LocalDate.parse("2016-03-01", ISODateTimeFormat.date())
      Days.daysBetween(yearBeforeFeb29, yearAfterFeb29).getDays shouldBe 366
      AgentEpayeRegistrationValidator.isValidDateRange(yearBeforeFeb29, yearAfterFeb29) shouldBe Valid(())
    }
    "fail if given a null parameter" in {
      val somedate = LocalDate.parse("2001-01-01", ISODateTimeFormat.date())
      val expectedCode = "INVALID_DATE_RANGE"
      val expectedMsg = "Both 'To' and 'From' dates are required"
      AgentEpayeRegistrationValidator.isValidDateRange(null, somedate) shouldBe anError(expectedCode, expectedMsg)
      AgentEpayeRegistrationValidator.isValidDateRange(somedate, null) shouldBe anError(expectedCode, expectedMsg)
      AgentEpayeRegistrationValidator.isValidDateRange(null, null) shouldBe anError(expectedCode, expectedMsg)
    }
  }

  "validateDateRange captures all classes of date range validation errors" when {
    val present = LocalDate.now(DateTimeZone.UTC)
    val past = present.minusDays(1)
    "param is not in the past" in {
      validateDateRange(present, past) shouldBe anError("INVALID_DATE_RANGE", "'From' date must be in the past")
      validateDateRange(past, present) shouldBe anError("INVALID_DATE_RANGE", "'To' date must be in the past")
    }
    "from param is not after to param" in {
      validateDateRange(past, past.minusDays(1)) shouldBe anError("INVALID_DATE_RANGE", "'To' date must be after 'From' date")
    }
    "date range is less than or equal to a year" in {
      validateDateRange(past.minusYears(1).minusDays(1), past) shouldBe anError("INVALID_DATE_RANGE", "Date range must be 1 year or less")
    }
  }

  "validateRegistrationRequest should check all mandatory fields are present" when {
    "an agent name is not given" in {
      validateRegistrationRequest(regRequest.copy(agentName = " ")) shouldBe
        anError("MISSING_FIELD", "The agent name field is mandatory")
    }
    "a contact name is not given" in {
      validateRegistrationRequest(regRequest.copy(contactName = " ")) shouldBe
        anError("MISSING_FIELD", "The contact name field is mandatory")
    }
    "the first address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine1 = " "))) shouldBe
        anError("MISSING_FIELD", "The address line 1 field is mandatory")
    }
    "the second address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine2 = " "))) shouldBe
        anError("MISSING_FIELD", "The address line 2 field is mandatory")
    }
    "the postcode is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(postCode = " "))) shouldBe
        anError("MISSING_FIELD", "The postcode field is mandatory")
    }
  }

  "validateRegistrationRequest should pass if an optional field is missing" when {
    "the optional telephone number is not given" in {
      validateRegistrationRequest(regRequest.copy(telephoneNumber = None)) shouldBe Valid(())
    }
    "the optional fax number is not given" in {
      validateRegistrationRequest(regRequest.copy(faxNumber = None)) shouldBe Valid(())
    }
    "the optional third address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine3 = None))) shouldBe Valid(())
    }
    "the optional fourth address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine4 = None))) shouldBe Valid(())
    }
  }

  "validateRegistrationRequest checks the email address field really is an email address" when {
    "an invalid email address is given" in {
      validateRegistrationRequest(regRequest.copy(emailAddress = Some("a.c.com"))) shouldBe
        Invalid(Failure("INVALID_FIELD", "The email address field is not a valid email"))
    }
  }

  "validateRegistrationRequest checks alphanumeric fields contain acceptable characters" when {
    "an invalid agent name is given" in {
      validateRegistrationRequest(regRequest.copy(agentName = """Jonny "John" Jones""")) shouldBe
        Invalid(Failure("INVALID_FIELD", "The agent name field contains invalid characters"))
    }
    "an invalid contact name is given" in {
      validateRegistrationRequest(regRequest.copy(contactName = """Jonny "John" Jones""")) shouldBe
        Invalid(Failure("INVALID_FIELD", "The contact name field contains invalid characters"))
    }
    "an invalid first address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine1 = "~"))) shouldBe
        anError("INVALID_FIELD", "The address line 1 field contains invalid characters")
    }
    "an invalid second address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine2 = "~"))) shouldBe
        anError("INVALID_FIELD", "The address line 2 field contains invalid characters")
    }
    "an invalid third address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine3 = Some("~")))) shouldBe
        anError("INVALID_FIELD", "The address line 3 field contains invalid characters")
    }
    "an invalid fourth address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine4 = Some("~")))) shouldBe
        anError("INVALID_FIELD", "The address line 4 field contains invalid characters")
    }
  }

  "validateRegistrationRequest checks the phone numbers are in an acceptable format" when {
    "an invalid telephone number is given" in {
      validateRegistrationRequest(regRequest.copy(telephoneNumber = Some("~"))) shouldBe
        anError("INVALID_FIELD", "The telephone number field is not a valid phone number")
    }
    "an invalid fax number is given" in {
      validateRegistrationRequest(regRequest.copy(faxNumber = Some("~"))) shouldBe
        anError("INVALID_FIELD", "The fax number field is not a valid phone number")
    }
  }

  "validateRegistrationRequest checks the postcode really is a postcode" when {
    "an invalid postcode is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(postCode = "~"))) shouldBe
        anError("INVALID_FIELD", "The postcode field is not a valid postcode")
    }
  }

  "validateRegistrationRequest should pass when all values are valid" in {
    validateRegistrationRequest(regRequest) shouldBe Valid(())
  }

  "validateRegistrationRequest reports all errors if multiple fields are invalid" in {
    validateRegistrationRequest(regRequest.copy(agentName = " ", contactName = " ")) shouldBe
      Invalid(Failure(Set(
        ValidationError("MISSING_FIELD", "The agent name field is mandatory"),
        ValidationError("MISSING_FIELD", "The contact name field is mandatory")
      )))
  }

  "validateRegistrationRequest fails if a field's maximum length is exceeded" when {
    "the agent name is longer than 56 characters" in {
      validateRegistrationRequest(regRequest.copy(agentName = padField(57))) shouldBe
        anError("INVALID_FIELD", "The agent name field exceeds 56 characters")
    }
    "the contact name is longer than 56 characters" in {
      validateRegistrationRequest(regRequest.copy(contactName = padField(57))) shouldBe
        anError("INVALID_FIELD", "The contact name field exceeds 56 characters")
    }
    "the telephone number is longer than 24 characters" in {
      validateRegistrationRequest(regRequest.copy(telephoneNumber = Some(padField(25)))) shouldBe
        anError("INVALID_FIELD", "The telephone number field exceeds 24 characters")
    }
    "the fax number is longer than 24 characters" in {
      validateRegistrationRequest(regRequest.copy(faxNumber = Some(padField(25)))) shouldBe
        anError("INVALID_FIELD", "The fax number field exceeds 24 characters")
    }
    "the email address is longer than 129 characters" in {
      validateRegistrationRequest(regRequest.copy(emailAddress = Some(s"${padField(130)}@example.org"))) shouldBe
        anError("INVALID_FIELD", "The email address field exceeds 129 characters")
    }
    "the address line 1 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine1 = padField(36)))) shouldBe
        anError("INVALID_FIELD", "The address line 1 field exceeds 35 characters")
    }
    "the address line 2 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine2 = padField(36)))) shouldBe
        anError("INVALID_FIELD", "The address line 2 field exceeds 35 characters")
    }
    "the address line 3 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine3 = Some(padField(36))))) shouldBe
        anError("INVALID_FIELD", "The address line 3 field exceeds 35 characters")
    }
    "the address line 4 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine4 = Some(padField(36))))) shouldBe
        anError("INVALID_FIELD", "The address line 4 field exceeds 35 characters")
    }
  }
}
