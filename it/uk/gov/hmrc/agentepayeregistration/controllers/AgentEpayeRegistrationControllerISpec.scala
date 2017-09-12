package uk.gov.hmrc.agentepayeregistration.controllers

import org.joda.time._
import play.api.libs.json.{JsObject, JsString, Json}
import uk.gov.hmrc.agentepayeregistration.models.{Address, RegistrationRequest}
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
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
          val result = getRegistrations("2001-01-01", "2001-01-01")
          result.status shouldBe 200
          result.header("Content-Type") shouldBe Some("application/json")
          Json.parse(result.body) shouldBe Json.parse("""{ "registrations" : [] }""")
        }
      }

      "respond 200 OK with registrations in JSON" when {
        "request contains valid parameters and registrations are present in repository" in {
          val creationTime = DateTime.now(DateTimeZone.UTC).minusDays(1)
          val repo = app.injector.instanceOf[AgentEpayeRegistrationRepository]

          val regReq = RegistrationRequest("", "", None, None, None, Address("", "", None, None, ""))
          import scala.concurrent.ExecutionContext.Implicits.global
          await(repo.create(regReq, creationTime))

          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication")
          val dateParam = creationTime.toLocalDate.toString
          val result = getRegistrations(dateParam, dateParam)
          result.status shouldBe 200

          val responseJson = Json.parse(result.body).toString()
          responseJson should include("HX2000")
          responseJson should include(creationTime.toString)
        }}

      "respond 403 Forbidden" when {
        "request is authenticated but not with expected stride enrolment" in {
          givenAuthorisedFor("OtherStrideEnrolment", "PrivilegedApplication")
          val result = getRegistrations("2001-01-01", "2001-01-01")
          result.status shouldBe 403
        }
      }

      "respond 401 Unauthorised" when {
        "request is not authorised for a MDTP detail - NoActiveSession" in {
          givenRequestIsNotAuthorised("MissingBearerToken")
          val result = getRegistrations("2001-01-01", "2001-01-01")
          result.status shouldBe 401
        }
      }

      "respond 400 Bad Request" when {
        "date range parameters are not in ISO date format" in {
          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication")
          val result = getRegistrations("01-01-2001", "01-01-2001")
          result.status shouldBe 400
          result.header("Content-Type") shouldBe Some("application/json")

          result.body should include("'From' date must be in ISO format (yyyy-MM-dd)")
        }

        "date validation fails" in {
          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication")
          val result = getRegistrations("2001-01-02", "2001-01-01")
          result.status shouldBe 400
          result.header("Content-Type") shouldBe Some("application/json")
          Json.parse(result.body) shouldBe Json.parse(
            """{
                "errors" : [
                  {
                    "code" : "INVALID_DATE_RANGE",
                    "message" : "'To' date must be after 'From' date"
                  }
                ]
              }""")
        }
      }
    }
  }
}
