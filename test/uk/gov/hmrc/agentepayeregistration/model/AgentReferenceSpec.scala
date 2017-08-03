package uk.gov.hmrc.agentepayeregistration.model

import uk.gov.hmrc.agentepayeregistration.models.AgentReference
import uk.gov.hmrc.play.test.UnitSpec

class AgentReferenceSpec extends UnitSpec {
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
      AgentReference("HX2345").newReference shouldBe AgentReference("HX2346")
    }

    "increment the alpha portion if the numeric portion has reached 9999" in {
      AgentReference("HX9999").newReference shouldBe AgentReference("HY0001")

      AgentReference("HZ9999").newReference shouldBe AgentReference("IA0001")
    }
  }
}
