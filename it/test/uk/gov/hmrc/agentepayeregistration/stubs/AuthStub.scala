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

package uk.gov.hmrc.agentepayeregistration.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.agentepayeregistration.support.WireMockSupport

trait AuthStub {
  me: WireMockSupport =>

  def givenRequestIsNotAuthorised(mdtpDetail: String): AuthStub = {
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("WWW-Authenticate", s"""MDTP detail="$mdtpDetail"""")
        )
    )
    this
  }

  def givenAuthorisedFor(enrolment: String, authProvider: String, strideUserId: String): AuthStub = {
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .atPriority(1)
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
           """.stripMargin,
            true,
            true
          )
        )
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(s"""{"authProviderId":{"paClientId":"$strideUserId"}}""")
        )
    )

    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .atPriority(2)
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")
        )
    )
    this
  }

}
