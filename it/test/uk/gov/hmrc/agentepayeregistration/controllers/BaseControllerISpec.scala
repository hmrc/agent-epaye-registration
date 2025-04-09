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

package uk.gov.hmrc.agentepayeregistration.controllers

import config.AppConfig
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentepayeregistration.stubs.DesStub
import uk.gov.hmrc.agentepayeregistration.support.{MongoApp, WireMockSupport}
import org.scalatestplus.play.PlaySpec

abstract class BaseControllerISpec
    extends PlaySpec
    with Eventually
    with GuiceOneServerPerSuite
    with MongoApp
    with WireMockSupport
    with DesStub {

  def additionalTestConfiguration: Seq[(String, Any)] = Seq.empty

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(4, Seconds), interval = Span(1, Seconds))

  override implicit lazy val app: Application = appBuilder.build()

  val config: AppConfig = app.injector.instanceOf[AppConfig]

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(mongoConfiguration)
      .configure(
        "microservice.services.auth.port"               -> wireMockPort,
        "microservice.services.des.host"                -> wireMockHost,
        "microservice.services.des.port"                -> wireMockPort,
        "microservice.services.des.environment"         -> "",
        "microservice.services.des.authorization-token" -> ""
      )
      .configure(additionalTestConfiguration: _*)

}
