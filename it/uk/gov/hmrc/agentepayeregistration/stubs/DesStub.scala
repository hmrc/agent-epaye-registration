package uk.gov.hmrc.agentepayeregistration.stubs

import uk.gov.hmrc.agentepayeregistration.models.AgentReference
import uk.gov.hmrc.agentepayeregistration.support.WireMockSupport
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.client.WireMock._

trait DesStub {
  me: WireMockSupport =>
  def createAgentKnownFactsValid(agentRef: AgentReference): Unit = {
    stubFor(post(urlEqualTo(s"/agents/regime/PAYE/agentid/${agentRef.value}/known-facts"))
      .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(equalToJson(s"""{
                                  |  "agentName": "Alex P",
                                  |  "contactName": "Alex P",
                                  |  "addressLine1": "6 High Street",
                                  |  "addressLine2": "Kidderminster",
                                  |  "postCode": "TF3 4ER",
                                  |  "phoneNo": "199999",
                                  |  "faxNumber": "11 111 0000",
                                  |  "email": "a@a.com",
                                  |  "createdDate": "2000-01-01"
                                  }""".stripMargin, true, true))
      .willReturn(aResponse().withStatus(204)))
  }

  def createAgentKnownFactsInvalidAgentId(agentRef: AgentReference) = {
    stubFor(post(urlPathEqualTo(s"/agents/regime/PAYE/agentid/${agentRef.value}/known-facts"))
      .withHeader("Content-Type", containing("application/json"))
      .withRequestBody(equalToJson(s"""{
                                  |  "agentName": "Alex P",
                                  |  "contactName": "Alex P",
                                  |  "addressLine1": "6 High Street",
                                  |  "addressLine2": "Kidderminster",
                                  |  "postCode": "TF3 4ER",
                                  |  "phoneNo": "199999",
                                  |  "faxNumber": "11 111 0000",
                                  |  "email": "a@a.com",
                                  |  "createdDate": "2000-01-01"
                                  }""".stripMargin, true, true))
      .willReturn(aResponse().withStatus(400)))
  }

  def createAgentKnownFactsInvalidRegime(agentRef: AgentReference) = {
    stubFor(post(urlPathEqualTo(s"/agents/regime/AAA/agentid/${agentRef.value}/known-facts"))
      .withHeader("Content-Type", containing("application/json"))
      .withRequestBody(equalToJson(s"""{
                                  |  "agentName": "Alex P",
                                  |  "contactName": "Alex P",
                                  |  "addressLine1": "6 High Street",
                                  |  "addressLine2": "Kidderminster",
                                  |  "postCode": "TF3 4ER",
                                  |  "phoneNo": "199999",
                                  |  "faxNumber": "11 111 0000",
                                  |  "email": "a@a.com",
                                  |  "createdDate": "2000-01-01"
                                  }""".stripMargin, true, true))
      .willReturn(aResponse().withStatus(400)))
  }

  def createAgentKnownFactsInvalidBoth = {
    stubFor(post(urlPathEqualTo("/agents/regime/AAA/agentid/ZZ0000/known-facts"))
      .withHeader("Content-Type", containing("application/json"))
      .withRequestBody(equalToJson(s"""{
                                  |  "agentName": "Alex P",
                                  |  "contactName": "Alex P",
                                  |  "addressLine1": "6 High Street",
                                  |  "addressLine2": "Kidderminster",
                                  |  "postCode": "TF3 4ER",
                                  |  "phoneNo": "199999",
                                  |  "faxNumber": "11 111 0000",
                                  |  "email": "a@a.com",
                                  |  "createdDate": "2000-01-01"
                                  }""".stripMargin, true, true))
      .willReturn(aResponse().withStatus(400)))
  }

  def createAgentKnownFactsInvalidPayload(agentRef: AgentReference) = {
    stubFor(post(urlPathEqualTo(s"/agents/regime/PAYE/agentid/${agentRef.value}/known-facts"))
      .withHeader("Content-Type", containing("application/json"))
      .withRequestBody(equalToJson(s"""{
                                  |  "agentName": "'INVALID PAYLOAD'",
                                  |  "contactName": "Alex P",
                                  |  "addressLine1": "6 High Street",
                                  |  "addressLine2": "Kidderminster",
                                  |  "postCode": "TF3 4ER",
                                  |  "phoneNo": "199999",
                                  |  "faxNumber": "11 111 0000",
                                  |  "email": "a@a.com",
                                  |  "createdDate": "2000-01-01"
                                  }""".stripMargin, true, true))
      .willReturn(aResponse().withStatus(400)))
  }

  def createAgentKnownFactsFailsWithStatus(agentRef: AgentReference, status: Int) = {
    stubFor(post(urlPathEqualTo(s"/agents/regime/PAYE/agentid/${agentRef.value}/known-facts"))
      .willReturn(aResponse().withStatus(status)))
  }

  def givenAgentKnownFactsComplete(agentRef: AgentReference): Unit = {
    stubFor(post(urlEqualTo(s"/agents/regime/PAYE/agentid/${agentRef.value}/known-facts"))
      .withHeader("Content-Type", equalTo("application/json"))
      .withRequestBody(equalToJson(
        s"""{
           |  "agentName": "Jim Jiminy",
           |  "contactName": "John Johnson",
           |  "addressLine1": "Line 1",
           |  "addressLine2": "Line 2",
           |  "addressLine3": "Line 3",
           |  "addressLine4": "Line 4",
           |  "postCode": "AB111AA",
           |  "phoneNo": "12345",
           |  "faxNumber": "12345",
           |  "email": "john.smith@email.com"
        }""".stripMargin, true, true))
      .willReturn(aResponse().withStatus(204)))
  }

  def givenAgentKnownFactsIncomplete(agentRef: AgentReference): Unit = {
    stubFor(post(urlEqualTo(s"/agents/regime/PAYE/agentid/${agentRef.value}/known-facts"))
      .withHeader("Content-Type", equalTo("application/json"))
      .withRequestBody(equalToJson(
        s"""{
           |  "agentName": "Jim Jiminy",
           |  "contactName": "John Johnson",
           |  "addressLine1": "Line 1",
           |  "addressLine2": "Line 2",
           |  "postCode": "AB111AA"
        }""".stripMargin, true, true))
      .willReturn(aResponse().withStatus(204)))
  }
}
