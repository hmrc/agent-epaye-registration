package uk.gov.hmrc.agentepayeregistration.controllers

import play.api.libs.json.{JsObject, JsString, Json}
import uk.gov.hmrc.agentepayeregistration.stubs.AuthStub
import uk.gov.hmrc.agentepayeregistration.support.RegistrationActions

class AgentEpayeRegistrationControllerISpec extends BaseControllerISpec with AuthStub with RegistrationActions {

  override def additionalTestConfiguration: Seq[(String, String)] = Seq(
    "extract.auth.stride.enrolment" -> "ValidStrideEnrolment"
  )

  val validPostData: JsObject = Json.obj(
    "agentName" -> "Jim Jiminy",
    "contactName" -> "John Johnson",
    "address" -> Json.obj(
      "addressLine1" -> "Line 1",
      "addressLine2" -> "Line 2",
      "postCode" -> "AB111AA"
    )
  )

  "RegistrationController" when {

    "POST /registrations with valid details" should {
      "respond HTTP 200 with a the new unique PAYE code in the response body" in {
        val result = postRegistration(validPostData)

        result.status shouldBe 200
        result.json shouldBe Json.obj("payeAgentReference" -> "HX2000")
      }
    }

    "POST /registrations with invalid details" should {
      "respond with HTTP 400 Bad Request" when {

        "no details are given" in {
          val result = postRegistration(Json.obj())
          result.status shouldBe 400
        }

        "some mandatory field was missing" in {
          val postDataMissingField = validPostData - "agentName"

          val result = postRegistration(postDataMissingField)

          result.status shouldBe 400
        }

        "some field was invalid" in {
          val postDataInvalidField = validPostData + ("agentName" -> JsString("Invalid#Name"))

          val result = postRegistration(postDataInvalidField)

          result.status shouldBe 400
        }
      }
    }

    "GET /registrations" should {

      "respond 200 OK" when {
        "request is authorised by expected stride enrolment" in {
          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication")
          val result = getRegistrations
          result.status shouldBe 200
        }
      }

      "respond 403 Forbidden" when {
        "request is authenticated but not with expected stride enrolment" in {
          givenAuthorisedFor("OtherStrideEnrolment", "PrivilegedApplication")
          val result = getRegistrations
          result.status shouldBe 403
        }
      }

      "respond 401 Unauthorised" when {
        "request is not authorised for a MDTP detail - NoActiveSession" in {
          givenRequestIsNotAuthorised("MissingBearerToken")
          val result = getRegistrations
          result.status shouldBe 401
        }
      }

    }
  }
}
