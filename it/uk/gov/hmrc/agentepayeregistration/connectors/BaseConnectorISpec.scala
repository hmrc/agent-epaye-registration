package uk.gov.hmrc.agentepayeregistration.connectors

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentepayeregistration.stubs.DesStub
import uk.gov.hmrc.agentepayeregistration.support.WireMockSupport
import uk.gov.hmrc.play.test.UnitSpec

class BaseConnectorISpec extends UnitSpec with GuiceOneServerPerSuite with WireMockSupport with DesStub  {

  def additionalTestConfiguration: Seq[(String, Any)] = Seq.empty

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.des.host" -> wireMockHost,
        "microservice.services.des.port" -> wireMockPort,
        "microservice.services.des.environment" -> "",
        "microservice.services.des.authorization-token" -> ""
      )
      .configure(additionalTestConfiguration:_*)
  }
}
