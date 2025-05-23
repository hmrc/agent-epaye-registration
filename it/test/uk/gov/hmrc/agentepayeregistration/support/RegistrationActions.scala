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

package uk.gov.hmrc.agentepayeregistration.support

import org.scalatest.Suite
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.HeaderNames

trait RegistrationActions extends ScalaFutures {

  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port/agent-epaye-registration"

  val wsClient = app.injector.instanceOf[WSClient]

  def postRegistration(registrationRequest: JsValue): WSResponse =
    wsClient
      .url(s"$url/registrations")
      .post(registrationRequest)
      .futureValue

  def getRegistrations(urlEncodedDateFrom: String, urlEncodedDateTo: String): WSResponse =
    wsClient
      .url(s"$url/registrations?dateFrom=$urlEncodedDateFrom&dateTo=$urlEncodedDateTo")
      .withHttpHeaders(HeaderNames.authorisation -> "Bearer XYZ")
      .get()
      .futureValue

}
