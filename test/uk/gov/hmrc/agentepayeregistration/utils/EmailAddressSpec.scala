/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

import javax.naming.NamingException
import javax.naming.directory.{BasicAttributes, DirContext}

class EmailAddressSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  private val mailExchangeRecordType = "MX"
  private val addressRecordType      = "A"

  class Harness {
    val context: DirContext = mock[DirContext]
    val validator: EmailAddressValidation = new EmailAddressValidation {
      override protected def dnsContext: DirContext = context
    }

    def verifyValidationAndDnsLookups(expectedResult: Boolean): Unit = {
      validator.isValid("name@example.com") mustEqual expectedResult

      verify(context).getAttributes("example.com", Array(mailExchangeRecordType))
      verify(context).getAttributes("example.com", Array(addressRecordType))
    }

    def stubLookup(recordType: String, outcome: String): Unit =
      outcome match {
        case "present" =>
          val exampleMailServer  = "10 mail.example.com"
          val exampleIpv4Address = "192.0.2.1"
          val value = if (recordType == mailExchangeRecordType) exampleMailServer else exampleIpv4Address
          when(context.getAttributes("example.com", Array(recordType)))
            .thenReturn(new BasicAttributes(recordType, value))
        case "absent" =>
          when(context.getAttributes("example.com", Array(recordType))).thenReturn(new BasicAttributes())
        case _ =>
          when(context.getAttributes("example.com", Array(recordType))).thenThrow(new NamingException("Lookup failed"))
      }
  }

  "isValid" - {

    "when checking DNS records for a correctly formatted email address" - {

      "must accept the email when both mail exchange and address records exist" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "present")
        harness.stubLookup(addressRecordType, "present")

        harness.verifyValidationAndDnsLookups(expectedResult = true)
      }

      "must accept the email when a mail exchange record exists but no address record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "present")
        harness.stubLookup(addressRecordType, "absent")

        harness.verifyValidationAndDnsLookups(expectedResult = true)
      }

      "must accept the email when a mail exchange record exists and the address lookup fails" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "present")
        harness.stubLookup(addressRecordType, "failed")

        harness.verifyValidationAndDnsLookups(expectedResult = true)
      }

      "must accept the email when no mail exchange record exists but an address record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "absent")
        harness.stubLookup(addressRecordType, "present")

        harness.verifyValidationAndDnsLookups(expectedResult = true)
      }

      "must reject the email when neither mail exchange nor address records exist" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "absent")
        harness.stubLookup(addressRecordType, "absent")

        harness.verifyValidationAndDnsLookups(expectedResult = false)
      }

      "must reject the email when no mail exchange record exists and the address lookup fails" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "absent")
        harness.stubLookup(addressRecordType, "failed")

        harness.verifyValidationAndDnsLookups(expectedResult = false)
      }

      "must accept the email when the mail exchange lookup fails but an address record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "failed")
        harness.stubLookup(addressRecordType, "present")

        harness.verifyValidationAndDnsLookups(expectedResult = true)
      }

      "must reject the email when the mail exchange lookup fails and no address record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "failed")
        harness.stubLookup(addressRecordType, "absent")

        harness.verifyValidationAndDnsLookups(expectedResult = false)
      }

      "must reject the email when both DNS lookups fail" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "failed")
        harness.stubLookup(addressRecordType, "failed")

        harness.verifyValidationAndDnsLookups(expectedResult = false)
      }
    }

    "supported email characters" - {

      "must accept letters and numbers before the @ symbol when a mail exchange record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "present")
        harness.stubLookup(addressRecordType, "absent")

        harness.validator.isValid("Test123@example.com") mustEqual true
      }

      "must accept a dot before the @ symbol when a mail exchange record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "present")
        harness.stubLookup(addressRecordType, "absent")

        harness.validator.isValid("first.lastName@example.com") mustEqual true
      }

      "must accept a plus sign before the @ symbol when a mail exchange record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "present")
        harness.stubLookup(addressRecordType, "absent")

        harness.validator.isValid("name+tag@example.com") mustEqual true
      }

      "must accept supported special characters before the @ symbol when a mail exchange record exists" in {
        val harness = new Harness
        harness.stubLookup(mailExchangeRecordType, "present")
        harness.stubLookup(addressRecordType, "absent")

        harness.validator.isValid("!#$%&’'*+/=?^_`{|}~-@example.com") mustEqual true
      }
    }

    "incorrect email address" - {

      "must reject without looking up DNS when the email is empty" in {
        val harness = new Harness

        harness.validator.isValid("") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when only a name is supplied" in {
        val harness = new Harness

        harness.validator.isValid("name") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the @ symbol is missing" in {
        val harness = new Harness

        harness.validator.isValid("name.example.com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the part before the @ symbol is missing" in {
        val harness = new Harness

        harness.validator.isValid("@example.com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the domain is missing" in {
        val harness = new Harness

        harness.validator.isValid("name@") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when there is more than one @ symbol" in {
        val harness = new Harness

        harness.validator.isValid("name@@example.com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when there is a space before the @ symbol" in {
        val harness = new Harness

        harness.validator.isValid("first last@example.com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the domain contains a space" in {
        val harness = new Harness

        harness.validator.isValid("name@exam ple.com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the domain contains an underscore" in {
        val harness = new Harness

        harness.validator.isValid("name@example_com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the domain starts with a dot" in {
        val harness = new Harness

        harness.validator.isValid("name@.example.com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the domain contains consecutive dots" in {
        val harness = new Harness

        harness.validator.isValid("name@example..com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the domain ends with a dot" in {
        val harness = new Harness

        harness.validator.isValid("name@example.com.") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the email has a leading space" in {
        val harness = new Harness

        harness.validator.isValid(" name@example.com") mustEqual false

        verifyNoInteractions(harness.context)
      }

      "must reject without looking up DNS when the email has a trailing space" in {
        val harness = new Harness

        harness.validator.isValid("name@example.com ") mustEqual false

        verifyNoInteractions(harness.context)
      }
    }
  }
}
