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
    wsClient.url(s"$url/registrations")
      .post(registrationRequest)
      .futureValue

  def getRegistrations(urlEncodedDateFrom: String, urlEncodedDateTo: String): WSResponse = {
    wsClient.url(s"$url/registrations?dateFrom=$urlEncodedDateFrom&dateTo=$urlEncodedDateTo")
      .withHttpHeaders(HeaderNames.authorisation -> "Bearer XYZ")
      .get()
      .futureValue
  }
}
