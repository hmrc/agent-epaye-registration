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

package uk.gov.hmrc.agentepayeregistration.model

import uk.gov.hmrc.agentepayeregistration.models.Address
import org.scalatestplus.play.PlaySpec

class AddressSpec extends PlaySpec {

  val testAddress: Address = new Address(
    "addressLine1",
    "addressLine2",
    Some("addressLine3"),
    Some("addressLine4"),
    "postCode"
  )


  val testAddress2: Address = new Address(
    "addressLine1",
    "addressLine2",
    None,
    None,
    "postCode"
  )

  val testAddress3: Address = new Address(
    "",
    "",
    None,
    None,
    ""
  )

  "toString" should {
    "return Address as String" in {
      val actualAddress = testAddress.toString()

      val expectedAddressString: String = "addressLine1 addressLine2 addressLine3 addressLine4 postCode"

      actualAddress mustBe expectedAddressString
    }

    "return Address with empty optional fields as String" in {
      val actualAddress = testAddress2.toString()

      val expectedAddressString: String = "addressLine1 addressLine2 postCode"

      actualAddress mustBe expectedAddressString
    }

    "return empty Address with empty optional fields as String" in {
      val actualAddress = testAddress3.toString()

      val expectedAddressString: String = ""

      actualAddress mustBe expectedAddressString
    }
  }
}
