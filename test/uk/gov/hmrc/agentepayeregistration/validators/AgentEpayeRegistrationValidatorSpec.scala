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

import cats.data.Validated.{Invalid, Valid}

import java.time.format.DateTimeFormatter.ISO_DATE
import java.time.{LocalDate, ZoneId, ZoneOffset}
import uk.gov.hmrc.agentepayeregistration.models.{Address, Failure, RegistrationRequest, ValidationError}
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.agentepayeregistration.validators.AgentEpayeRegistrationValidator._

import java.time.temporal.ChronoUnit

class AgentEpayeRegistrationValidatorSpec extends PlaySpec {
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
      AgentEpayeRegistrationValidator.nonEmpty("")("sample") mustBe expected
      AgentEpayeRegistrationValidator.nonEmpty(" ")("sample") mustBe expected
      AgentEpayeRegistrationValidator.nonEmpty("\t")("sample") mustBe expected
      AgentEpayeRegistrationValidator.nonEmpty("   \n")("sample") mustBe expected
    }

    "allow with non-whitespace" in {
      AgentEpayeRegistrationValidator.nonEmpty("s")("sample") mustBe Valid(())
      AgentEpayeRegistrationValidator.nonEmpty(" s")("sample") mustBe Valid(())
      AgentEpayeRegistrationValidator.nonEmpty("s ")("sample") mustBe Valid(())
      AgentEpayeRegistrationValidator.nonEmpty("s\ts\n")("sample") mustBe Valid(())
    }
  }

  "isValidCharacters validator" should {
    "disallow invalid characters" in {
      val expectedInvalid = Invalid(Failure("INVALID_FIELD", s"The sample field contains invalid characters"))
      AgentEpayeRegistrationValidator.isValidCharacters("~")("sample") mustBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("[")("sample") mustBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("#")("sample") mustBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("&")("sample") mustBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("'")("sample") mustBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("\\")("sample") mustBe expectedInvalid
      AgentEpayeRegistrationValidator.isValidCharacters("/")("sample") mustBe expectedInvalid
    }

    "allow a-z" in {
      AgentEpayeRegistrationValidator.nonEmpty("abcdefghijklmnopqrstuvwxyz")("sample") mustBe Valid(())
    }
    "allow A-Z" in {
      AgentEpayeRegistrationValidator.nonEmpty("ABCDEFGHIJKLMNOPQRSTUVWXYZ")("sample") mustBe Valid(())
    }
    "allow 0-9" in {
      AgentEpayeRegistrationValidator.nonEmpty("01234567890")("sample") mustBe Valid(())
    }
    "allow commas, periods, -, (, ), !, @ and space" in {
      AgentEpayeRegistrationValidator.nonEmpty(",.-()!@ ")("sample") mustBe Valid(())
    }
  }

  "maxLength validator" should {
    "pass if the length is less than the maximum" in {
      AgentEpayeRegistrationValidator.maxLength("1", 2)("x") mustBe Valid(())
    }
    "pass if the length is equal to the maximum" in {
      AgentEpayeRegistrationValidator.maxLength("22", 2)("x") mustBe Valid(())
    }
    "fail if the length is greater than the maximum" in {
      AgentEpayeRegistrationValidator.maxLength("333", 2)("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field exceeds 2 characters"))
    }
  }

  "isEmailAddress validator" should {
    "allow a simple email address" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a@domain.com")("x") mustBe Valid(())
    }

    "allow email addresses with a hyphen, period, numbers, plus, underscore, exclamation, number sign or question mark" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a-b@domain.com")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a.b@domain.com")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("1@domain.com")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a+b@domain.com")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a_b@domain.com")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a!b@domain.com")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a#b@domain.com")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isEmailAddress("a?b@domain.com")("x") mustBe Valid(())
    }

    "not allow an email address with a comma, colon, semicolon, parenthesis, pound sign or backslash" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a,b@b.com")("x") mustBe Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
      AgentEpayeRegistrationValidator.isEmailAddress("a:b@b.com")("x") mustBe Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
      AgentEpayeRegistrationValidator.isEmailAddress("a;b@b.com")("x") mustBe Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
      AgentEpayeRegistrationValidator.isEmailAddress("a(b@b.com")("x") mustBe Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
      AgentEpayeRegistrationValidator.isEmailAddress("a)b@b.com")("x") mustBe Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
      AgentEpayeRegistrationValidator.isEmailAddress("a£b@b.com")("x") mustBe Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
      AgentEpayeRegistrationValidator.isEmailAddress("a\\b@b.com")("x") mustBe Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }

    "not allow an email address without an @ symbol" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a.com")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }

    "not allow an email address with more than one @ symbol" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a@b@c.com")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }

    "not allow an email address without a domain" in {
      AgentEpayeRegistrationValidator.isEmailAddress("a@")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }

    "not allow an email address without a local-part" in {
      AgentEpayeRegistrationValidator.isEmailAddress("@b.com")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid email"))
    }
  }

  "isPostcode validator" should {
    "pass the 6 main formats of postcodes" in {
      AgentEpayeRegistrationValidator.isPostcode("AA9A9AA")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("A9A9AA")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("A99AA")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("A999AA")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("AA99AA")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("AA999AA")("x") mustBe Valid(())
    }

    "pass BFPO codes" in {
      AgentEpayeRegistrationValidator.isPostcode("BFPO1")("x") mustBe Valid(())
      AgentEpayeRegistrationValidator.isPostcode("BFPO1234")("x") mustBe Valid(())
    }

    "fail a valid postcode with lowercase characters" in {
      AgentEpayeRegistrationValidator.isPostcode("aa999aa")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid postcode"))
    }

    "fail any code that isn't one of the 6 main formats of postcodes" in {
      AgentEpayeRegistrationValidator.isPostcode("A99999A")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid postcode"))
    }

    "fail a code that is longer than 8 characters" in {
      AgentEpayeRegistrationValidator.isPostcode("AA9A9AAAA")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid postcode"))
    }
  }

  "isPhoneNumber validator" should {
    "pass just numbers" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("01234567890")("x") mustBe Valid(())
    }
    "pass numbers separated by a space" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("01234 567890")("x") mustBe Valid(())
    }
    "fail numbers with hash" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("#01234 567890")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid phone number"))
    }
    "pass numbers with an x" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("01234 567890x2")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid phone number"))
    }
    "pass with a country code prefix" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("00441234 567890")("x") mustBe Valid(())
    }
    "fail with a plus" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("+441234 567890")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid phone number"))
    }
    "fail with hyphens" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("44-1234-567890")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid phone number"))
    }
    "fail with letters" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("44 1234 ACME12")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid phone number"))
    }
    "fail with brackets" in {
      AgentEpayeRegistrationValidator.isPhoneNumber("(44) 123 345 567")("x") mustBe
        Invalid(Failure("INVALID_FIELD", "The x field is not a valid phone number"))
    }
  }

  "isInPast" should {
    "pass a date in the past" in {
      val dateYesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1)
      AgentEpayeRegistrationValidator.isInPast(dateYesterday)("x") mustBe Valid(())
    }
    "fail a date in the present" in {
      val dateToday = LocalDate.now(ZoneOffset.UTC)
      AgentEpayeRegistrationValidator.isInPast(dateToday)("x") mustBe
        Invalid(Failure("INVALID_DATE_RANGE", "'x' date must be in the past"))
    }
    "fail a date in the future" in {
      val dateTomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1)
      AgentEpayeRegistrationValidator.isInPast(dateTomorrow)("x") mustBe
        Invalid(Failure("INVALID_DATE_RANGE", "'x' date must be in the past"))
    }
  }

  "isValidDateRange" should {
    "pass a valid date range" in {
      val dateYesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1)
      AgentEpayeRegistrationValidator.isValidDateRange(dateYesterday, dateYesterday) mustBe Valid(())
      AgentEpayeRegistrationValidator.isValidDateRange(dateYesterday.minusDays(1), dateYesterday) mustBe Valid(())
    }
    "fail if the from date occurs after the to date" in {
      val dateYesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1)
      AgentEpayeRegistrationValidator.isValidDateRange(dateYesterday, dateYesterday.minusDays(1)) mustBe
        anError("INVALID_DATE_RANGE", "'To' date must be after 'From' date")
    }
    "fail if the date range spans more than a year" in {
      val someYear = LocalDate.parse("2015-03-01", ISO_DATE)
      AgentEpayeRegistrationValidator.isValidDateRange(someYear, someYear.plusYears(1).plusDays(1)) mustBe
        anError("INVALID_DATE_RANGE", "Date range must be 1 year or less")
    }
    "pass if the date range spans exactly 366 days of a leap year" in {
      val yearBeforeFeb29 = LocalDate.parse("2015-03-01", ISO_DATE)
      val yearAfterFeb29 = LocalDate.parse("2016-03-01", ISO_DATE)
      yearBeforeFeb29.until(yearAfterFeb29, ChronoUnit.DAYS) mustBe 366
      AgentEpayeRegistrationValidator.isValidDateRange(yearBeforeFeb29, yearAfterFeb29) mustBe Valid(())
    }
  }

  "validateDateRange captures all classes of date range validation errors" when {
    val present = LocalDate.now(ZoneOffset.UTC)
    val past = present.minusDays(1)
    "param is not in the past" in {
      validateDateRange(present, past) mustBe anError("INVALID_DATE_RANGE", "'From' date must be in the past")
      validateDateRange(past, present) mustBe anError("INVALID_DATE_RANGE", "'To' date must be in the past")
    }
    "from param is not after to param" in {
      validateDateRange(past, past.minusDays(1)) mustBe anError("INVALID_DATE_RANGE", "'To' date must be after 'From' date")
    }
    "date range is less than or equal to a year" in {
      validateDateRange(past.minusYears(1).minusDays(1), past) mustBe anError("INVALID_DATE_RANGE", "Date range must be 1 year or less")
    }
  }

  "validateRegistrationRequest should check all mandatory fields are present" when {
    "an agent name is not given" in {
      validateRegistrationRequest(regRequest.copy(agentName = " ")) mustBe
        anError("MISSING_FIELD", "The agent name field is mandatory")
    }
    "a contact name is not given" in {
      validateRegistrationRequest(regRequest.copy(contactName = " ")) mustBe
        anError("MISSING_FIELD", "The contact name field is mandatory")
    }
    "the first address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine1 = " "))) mustBe
        anError("MISSING_FIELD", "The address line 1 field is mandatory")
    }
    "the second address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine2 = " "))) mustBe
        anError("MISSING_FIELD", "The address line 2 field is mandatory")
    }
    "the postcode is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(postCode = " "))) mustBe
        anError("MISSING_FIELD", "The postcode field is mandatory")
    }
  }

  "validateRegistrationRequest should pass if an optional field is missing" when {
    "the optional telephone number is not given" in {
      validateRegistrationRequest(regRequest.copy(telephoneNumber = None)) mustBe Valid(())
    }
    "the optional fax number is not given" in {
      validateRegistrationRequest(regRequest.copy(faxNumber = None)) mustBe Valid(())
    }
    "the optional third address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine3 = None))) mustBe Valid(())
    }
    "the optional fourth address line is not given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine4 = None))) mustBe Valid(())
    }
  }

  "validateRegistrationRequest checks the email address field really is an email address" when {
    "an invalid email address is given" in {
      validateRegistrationRequest(regRequest.copy(emailAddress = Some("a.c.com"))) mustBe
        Invalid(Failure("INVALID_FIELD", "The email address field is not a valid email"))
    }
  }

  "validateRegistrationRequest checks alphanumeric fields contain acceptable characters" when {
    "an invalid agent name is given" in {
      validateRegistrationRequest(regRequest.copy(agentName = """Jonny "John" Jones""")) mustBe
        Invalid(Failure("INVALID_FIELD", "The agent name field contains invalid characters"))
    }
    "an invalid contact name is given" in {
      validateRegistrationRequest(regRequest.copy(contactName = """Jonny "John" Jones""")) mustBe
        Invalid(Failure("INVALID_FIELD", "The contact name field contains invalid characters"))
    }
    "an invalid first address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine1 = "~"))) mustBe
        anError("INVALID_FIELD", "The address line 1 field contains invalid characters")
    }
    "an invalid second address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine2 = "~"))) mustBe
        anError("INVALID_FIELD", "The address line 2 field contains invalid characters")
    }
    "an invalid third address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine3 = Some("~")))) mustBe
        anError("INVALID_FIELD", "The address line 3 field contains invalid characters")
    }
    "an invalid fourth address is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine4 = Some("~")))) mustBe
        anError("INVALID_FIELD", "The address line 4 field contains invalid characters")
    }
  }

  "validateRegistrationRequest checks the phone numbers are in an acceptable format" when {
    "an invalid telephone number is given" in {
      validateRegistrationRequest(regRequest.copy(telephoneNumber = Some("~"))) mustBe
        anError("INVALID_FIELD", "The telephone number field is not a valid phone number")
    }
    "an invalid fax number is given" in {
      validateRegistrationRequest(regRequest.copy(faxNumber = Some("~"))) mustBe
        anError("INVALID_FIELD", "The fax number field is not a valid phone number")
    }
  }

  "validateRegistrationRequest checks the postcode really is a postcode" when {
    "an invalid postcode is given" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(postCode = "~"))) mustBe
        anError("INVALID_FIELD", "The postcode field is not a valid postcode")
    }
  }

  "validateRegistrationRequest should pass when all values are valid" in {
    validateRegistrationRequest(regRequest) mustBe Valid(())
  }

  "validateRegistrationRequest reports all errors if multiple fields are invalid" in {
    validateRegistrationRequest(regRequest.copy(agentName = " ", contactName = " ")) mustBe
      Invalid(Failure(Set(
        ValidationError("MISSING_FIELD", "The agent name field is mandatory"),
        ValidationError("MISSING_FIELD", "The contact name field is mandatory")
      )))
  }

  "validateRegistrationRequest fails if a field's maximum length is exceeded" when {
    "the agent name is longer than 56 characters" in {
      validateRegistrationRequest(regRequest.copy(agentName = padField(57))) mustBe
        anError("INVALID_FIELD", "The agent name field exceeds 56 characters")
    }
    "the contact name is longer than 56 characters" in {
      validateRegistrationRequest(regRequest.copy(contactName = padField(57))) mustBe
        anError("INVALID_FIELD", "The contact name field exceeds 56 characters")
    }
    "the telephone number is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(telephoneNumber = Some(padField(36)))) mustBe
        anError("INVALID_FIELD", "The telephone number field exceeds 35 characters")
    }
    "the fax number is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(faxNumber = Some(padField(36)))) mustBe
        anError("INVALID_FIELD", "The fax number field exceeds 35 characters")
    }
    "the email address is longer than 129 characters" in {
      validateRegistrationRequest(regRequest.copy(emailAddress = Some(s"${padField(130)}@example.org"))) mustBe
        anError("INVALID_FIELD", "The email address field exceeds 129 characters")
    }
    "the address line 1 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine1 = padField(36)))) mustBe
        anError("INVALID_FIELD", "The address line 1 field exceeds 35 characters")
    }
    "the address line 2 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine2 = padField(36)))) mustBe
        anError("INVALID_FIELD", "The address line 2 field exceeds 35 characters")
    }
    "the address line 3 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine3 = Some(padField(36))))) mustBe
        anError("INVALID_FIELD", "The address line 3 field exceeds 35 characters")
    }
    "the address line 4 is longer than 35 characters" in {
      validateRegistrationRequest(regRequest.copy(address = address.copy(addressLine4 = Some(padField(36))))) mustBe
        anError("INVALID_FIELD", "The address line 4 field exceeds 35 characters")
    }
  }
}
