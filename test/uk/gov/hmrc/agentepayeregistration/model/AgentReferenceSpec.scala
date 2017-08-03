package uk.gov.hmrc.agentepayeregistration.model

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.agentepayeregistration.models.AgentReference

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
  }
}
