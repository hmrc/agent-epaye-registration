package uk.gov.hmrc.agentepayeregistration.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.agentepayeregistration.support.WireMockSupport

trait AuthStub {
  me: WireMockSupport =>

  def requestIsNotAuthenticated(): AuthStub = {
    stubFor(post(urlEqualTo("/auth/authorise")).willReturn(aResponse().withStatus(401)))
    this
  }

  def givenAuthorisedWithStride: AuthStub = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          s"""
             |{
             |  "authorise": [
             |    {
             |      "enrolment": "T2 Technical"
             |    },
             |    {
             |      "authProviders": ["PrivilegedApplication"]
             |    }
             |  ]
             |}
       """.stripMargin)))
    this
  }
}