package uk.gov.hmrc.agentepayeregistration.controllers

import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository

class AgentEpayeRegistrationControllerISpec extends BaseControllerISpec {
  private lazy val controller: AgentEpayeRegistrationController = app.injector.instanceOf[AgentEpayeRegistrationController]
  private lazy val repo = app.injector.instanceOf[AgentEpayeRegistrationRepository]

  val validPostData: JsObject = Json.obj(
    "agentName" -> "Jim Jiminy",
    "contactName" -> "John Johnson",
    "address" -> Json.obj(
      "addressLine1" -> "Line 1",
      "addressLine2" -> "Line 2",
      "postCode" -> "AB111AA"
    )
  )

  "submitting valid details to /registrations" should {
    "respond with HTTP 200 with a the new unique PAYE code in the response body" in {
      val request = FakeRequest(POST, "/registrations", FakeHeaders(), validPostData)
      val result = await(controller.register(request))

      status(result) shouldBe 200
      contentAsJson(result) shouldBe Json.obj("payeAgentReference" -> "HX2000")
    }
  }

  "submitting invalid details to /registrations" should {
    "respond with HTTP 400 Bad Request" when {
      "no details are given" in {
        val request = FakeRequest(POST, "/registrations", FakeHeaders(), Json.obj())
        val result = await(controller.register(request))

        status(result) shouldBe 400
      }

      "some mandatory field was missing" in {
        val postDataMissingField = validPostData - "agentName"

        val request = FakeRequest(POST, "/registrations", FakeHeaders(), postDataMissingField)
        val result = await(controller.register(request))

        status(result) shouldBe 400
      }

      "some field was invalid" in {
        val postDataInvalidField = validPostData + ("agentName", JsString("Invalid#Name"))

        val request = FakeRequest(POST, "/registrations", FakeHeaders(), postDataInvalidField)
        val result = await(controller.register(request))

        status(result) shouldBe 400
      }
    }
  }
}
