package uk.gov.hmrc.agentepayeregistration.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.agentepayeregistration.support.WireMockSupport

trait AuthStub {
  me: WireMockSupport =>

  def givenRequestIsNotAuthorised(mdtpDetail: String): AuthStub = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail="${mdtpDetail}"""")
      )
    )
    this
  }

  def givenAuthorisedFor(enrolment: String, authProvider: String, strideUserId: String): AuthStub = {
    stubFor(post(urlEqualTo("/auth/authorise")).atPriority(1)
      .withRequestBody(
        equalToJson(
          s"""
             |{
             |  "authorise": [
             |    {
             |      "enrolment": "$enrolment"
             |    },
             |    {
             |      "authProviders": [
             |        "$authProvider"
             |      ]
             |    }
             |  ]
             |}
           """.stripMargin, true, true))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type","application/json")
        .withBody(s"""{"authProviderId":{"paClientId":"$strideUserId"}}"""))
    )

    stubFor(post(urlEqualTo("/auth/authorise")).atPriority(2)
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")
      )
    )
    this
  }
}