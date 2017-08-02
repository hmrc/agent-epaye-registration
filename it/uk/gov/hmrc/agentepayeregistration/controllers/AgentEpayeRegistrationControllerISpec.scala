package uk.gov.hmrc.agentepayeregistration.controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers.{await, status, _}
import uk.gov.hmrc.agentepayeregistration.models.{Address, RegistrationDetails}
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.play.http.HeaderCarrier

class AgentEpayeRegistrationControllerISpec extends BaseControllerISpec {
  private lazy val controller: AgentEpayeRegistrationController = app.injector.instanceOf[AgentEpayeRegistrationController]
  private lazy val repo = app.injector.instanceOf[AgentEpayeRegistrationRepository]

  "submitting valid details to /register" should {
    "not require authentication" in {
      fail
    }

    "respond with HTTP 200 with a the new unique PAYE code in the response body" in {
      val postData = Json.parse(
        """
          |{
          |   "agentName" : "InvalidName",
          |   "contactName" : "John Johnson",
          |   "address" : {
          |       "addressLine1" : "Line 1",
          |       "addressLine2" : "Line 2",
          |       "postCode": "AB11 11A"
          |   }
          |}
        """.stripMargin)

      val request = FakeRequest(POST, "/register", FakeHeaders(), postData)
      val result = await(controller.register(request))

      status(result) shouldBe 200
      contentAsJson(result) shouldBe Json.obj("payeCode" -> "HX2000")
    }
  }

  "submitting invalid details to /register" should {
    "respond with HTTP 400 Bad Request" when {
      "no details are given" in {
        val request = FakeRequest(POST, "/register", FakeHeaders(), Json.obj())
        val result = await(controller.register(request))

        status(result) shouldBe 400
      }

      "some mandatory field was missing" in {
        val postData = Json.parse(
          """
            |{
            |   "contactName" : "John Johnson",
            |   "address" : {
            |       "addressLine1" : "Line 1",
            |       "addressLine2" : "Line 2",
            |       "postCode": "AB11 11A"
            |   }
            |}
          """.stripMargin)

        val request = FakeRequest(POST, "/register", FakeHeaders(), postData)
        val result = await(controller.register(request))

        status(result) shouldBe 400
      }

      "some field was invalid" in {
        val postData = Json.parse(
          """
            |{
            |   "agentName" : "InvalidName",
            |   "contactName" : "John Johnson",
            |   "address" : {
            |       "addressLine1" : "Line 1",
            |       "addressLine2" : "Line 2",
            |       "postCode": "AB11 11A"
            |   }
            |}
          """.stripMargin)

        val request = FakeRequest(POST, "/register", FakeHeaders(), postData)
        val result = await(controller.register(request))

        status(result) shouldBe 400
      }
    }
  }
}
