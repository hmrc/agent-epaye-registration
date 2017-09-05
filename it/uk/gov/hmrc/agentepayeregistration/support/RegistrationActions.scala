package uk.gov.hmrc.agentepayeregistration.support

import org.scalatest.Suite
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSResponse}

trait RegistrationActions extends ScalaFutures {

  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port/agent-epaye-registration"

  val wsClient = app.injector.instanceOf[WSClient]

  def postRegistration(registrationRequest: JsValue): WSResponse =
    wsClient.url(s"$url/registrations")
      .post(registrationRequest)
      .futureValue

  def getRegistrations: WSResponse =
    wsClient.url(s"$url/registrations")
      .get()
      .futureValue
}
