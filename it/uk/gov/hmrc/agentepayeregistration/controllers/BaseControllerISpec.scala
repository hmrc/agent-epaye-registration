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

abstract class BaseControllerISpec extends PlaySpec with Eventually with GuiceOneServerPerSuite with MongoApp with WireMockSupport with DesStub {

  def additionalTestConfiguration: Seq[(String, Any)] = Seq.empty

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(4, Seconds), interval = Span(1, Seconds))

  override implicit lazy val app: Application = appBuilder.build()

  val config: AppConfig = app.injector.instanceOf[AppConfig]

  protected def appBuilder: GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(mongoConfiguration)
      .configure(
        "microservice.services.auth.port" -> wireMockPort,
        "microservice.services.des.host" -> wireMockHost,
        "microservice.services.des.port" -> wireMockPort,
        "microservice.services.des.environment" -> "",
        "microservice.services.des.authorization-token" -> ""
      )
      .configure(additionalTestConfiguration:_*)
  }
}


