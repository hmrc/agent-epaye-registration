/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.agentepayeregistration.models.AgentReference
import org.scalatestplus.play.PlaySpec

class AgentReferenceSpec extends PlaySpec {
  "Validation on construction of an agent reference" should {
    "disallow an empty string" in {
      assertThrows[IllegalArgumentException] {
        AgentReference("")
      }
    }

    "disallow string of length other than 6" in {
      assertThrows[IllegalArgumentException] {
        AgentReference("HX20001")
      }
      assertThrows[IllegalArgumentException] {
        AgentReference("HX201")
      }
    }

    "disallow references which do not start with 2 uppercase alpha characters" in {
      assertThrows[IllegalArgumentException] {
        AgentReference("Hx2001")
      }

      assertThrows[IllegalArgumentException] {
        AgentReference("1X2001")
      }
    }

    "disallow references whose last four characters are not numeric" in {
      assertThrows[IllegalArgumentException] {
        AgentReference("HX2OO1")
      }
    }
  }

  "generating the next code" should {
    "increment just the numeric portion if it has not reached 9999" in {
      AgentReference("HX2345").newReference mustBe AgentReference("HX2346")
    }

    "increment the alpha portion if the numeric portion has reached 9999" in {
      AgentReference("HX9999").newReference mustBe AgentReference("HY0001")

      AgentReference("HZ9999").newReference mustBe AgentReference("IA0001")
    }
  }
}
