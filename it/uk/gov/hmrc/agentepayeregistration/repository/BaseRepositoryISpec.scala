/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.agentepayeregistration.repository

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentepayeregistration.support.{MongoApp, WireMockSupport}
import uk.gov.hmrc.play.test.UnitSpec

abstract class BaseRepositoryISpec extends UnitSpec with GuiceOneAppPerSuite with MongoApp with WireMockSupport {

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(mongoConfiguration)
  }
}
