package uk.gov.hmrc.agentepayeregistration.repository

import org.scalatestplus.play.OneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentepayeregistration.support.{MongoApp, WireMockSupport}
import uk.gov.hmrc.play.test.UnitSpec

abstract class BaseRepositoryISpec extends UnitSpec with OneAppPerSuite with MongoApp with WireMockSupport {

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(mongoConfiguration)
  }

}


