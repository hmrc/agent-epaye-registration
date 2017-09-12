package uk.gov.hmrc.agentepayeregistration.controllers

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentepayeregistration.support.{MongoApp, WireMockSupport}
import uk.gov.hmrc.play.test.UnitSpec

abstract class BaseControllerISpec extends WordSpecLike with UnitSpec with Matchers with Eventually with GuiceOneServerPerSuite with MongoApp with WireMockSupport {

  def additionalTestConfiguration: Seq[(String, String)] = Seq.empty

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(4, Seconds), interval = Span(1, Seconds))

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(mongoConfiguration)
      .configure(
        "microservice.services.auth.port" -> wireMockPort
      )
      .configure(additionalTestConfiguration:_*)
  }
}


