package uk.gov.hmrc.agentepayeregistration.controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, status, _}
import uk.gov.hmrc.play.http.HeaderCarrier

class AgentEpayeRegistrationControllerISpec extends BaseControllerISpec {
  private lazy val controller: AgentEpayeRegistrationController = app.injector.instanceOf[AgentEpayeRegistrationController]
  //private lazy val repo = app.injector.instanceOf[AgentEpayeRegistrationMongoRepository]

  "submitting valid details to /register" should {
    "not require authentication" in {
      fail
    }

    "create a new unique PAYE code in the repository" in {
      fail
    }

    "respond with HTTP 200 with a the new unique PAYE code in the response body" in {
      fail
    }
  }

  "submitting invalid details to /register" should {
    "respond with HTTP 400 Bad Request" when {
      "some field is bad" in {
        fail
      }
      "some other field is bad" in {
        fail
      }
    }

    "not create any new PAYE code in the repository" in {
      fail
    }
  }
}


