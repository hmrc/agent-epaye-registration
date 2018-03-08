package uk.gov.hmrc.agentepayeregistration.connectors

import uk.gov.hmrc.agentepayeregistration.models._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream5xxResponse}

import scala.concurrent.ExecutionContext.Implicits.global

class DesConnectorISpec extends BaseConnectorISpec {

  implicit val hc = HeaderCarrier()
  val connector = app.injector.instanceOf[DesConnector]

  val regRequest = RegistrationRequest("Alex P", "Alex P", Some("199999"), Some("11 111 0000"), Some("a@a.com"),
    Address("6 High Street", "Kidderminster", None, None, "TF3 4ER"))

  val invalidRegRequest = RegistrationRequest("'INVALID PAYLOAD'", "Alex P", Some("199999"), Some("11 111 0000"), Some("a@a.com"),
    Address("6 High Street", "Kidderminster", None, None, "TF3 4ER"))

  val createKnownFactsRequest = CreateKnownFactsRequest(regRequest, "2000-01-01")

  val createInvalidKnownFactsRequest = CreateKnownFactsRequest(invalidRegRequest, "2000-01-01")

  "createAgentKnownFacts" should {
    "return 204" in {
      createAgentKnownFactsValid(AgentReference("HX2001"))
      await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("HX2001"))).status shouldBe 204
    }

    "return 400 when AgentId is invalid" in {
      createAgentKnownFactsInvalidAgentId(AgentReference("ZZ0000"))
      an[BadRequestException] should be thrownBy {
        await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("ZZ0000")))
      }
    }

    "return 400 when regime is not PAYE" in {
      createAgentKnownFactsInvalidRegime(AgentReference("HX2001"))
      an[BadRequestException] should be thrownBy {
        await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("HX2001"), "AAA"))
      }
    }

    "return 400 when AgentId is invalid and the regime is not PAYE" in {
      createAgentKnownFactsInvalidBoth
      an[BadRequestException] should be thrownBy {
        await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("ZZ0000"), "AAA"))
      }
    }

    "return 400 when payload is invalid" in {
      createAgentKnownFactsInvalidPayload(AgentReference("HX2001"))
      an[BadRequestException] should be thrownBy {
        await(connector.createAgentKnownFacts(createInvalidKnownFactsRequest, AgentReference("HX2001")))
      }
    }

    "return 500 when DES is failing" in {
      createAgentKnownFactsFailsWithStatus(AgentReference("HX2001"), 503)
      an[Upstream5xxResponse] should be thrownBy {
        await(connector.createAgentKnownFacts(createKnownFactsRequest, AgentReference("HX2001")))
      }
    }
  }
}

