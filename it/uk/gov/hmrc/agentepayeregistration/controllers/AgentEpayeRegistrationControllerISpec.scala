package uk.gov.hmrc.agentepayeregistration.controllers

import org.joda.time._
import play.api.libs.json._
import uk.gov.hmrc.agentepayeregistration.audit.AgentEpayeRegistrationEvent
import uk.gov.hmrc.agentepayeregistration.models.{Address, RegistrationRequest}
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.agentepayeregistration.stubs.{AuthStub, DataStreamStub}
import uk.gov.hmrc.agentepayeregistration.support.RegistrationActions

class AgentEpayeRegistrationControllerISpec extends BaseControllerISpec with AuthStub with RegistrationActions with DataStreamStub {

  override def additionalTestConfiguration: Seq[(String, Any)] = Seq(
    "extract.auth.stride.enrolment" -> "ValidStrideEnrolment",
    "auditing.enabled" -> true,
    "auditing.consumer.baseUri.host" -> wireMockHost,
    "auditing.consumer.baseUri.port" -> wireMockPort
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

  val validPostDataComplete: JsObject = Json.obj(
    "agentName" -> "Jim Jiminy",
    "contactName" -> "John Johnson",
    "telephoneNumber" -> "12345",
    "faxNumber" -> "12345",
    "emailAddress" -> "john.smith@email.com",
    "address" -> Json.obj(
      "addressLine1" -> "Line 1",
      "addressLine2" -> "Line 2",
      "addressLine3" -> "Line 3",
      "addressLine4" -> "Line 4",
      "postCode" -> "AB111AA"
    )
  )

  "RegistrationController" when {

    "POST /registrations with valid details" should {
      "respond HTTP 200 with a the new unique PAYE code in the response body" in {
        givenAuditConnector()
        val result = postRegistration(validPostDataComplete)
        val requestPath: String = s"/agent-epaye-registration/registrations"

        result.status shouldBe 200
        result.json shouldBe Json.obj("payeAgentReference" -> "HX2000")

        verifyAuditRequestSent(1,
          event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated,
          detail = Map(
            "payeAgentRef" -> "HX2000",
            "agentName" -> "Jim Jiminy",
            "contactName" -> "John Johnson",
            "telephoneNumber" -> "12345",
            "faxNumber" -> "12345",
            "emailAddress" -> "john.smith@email.com",
            "addressLine1" -> "Line 1",
            "addressLine2" -> "Line 2",
            "addressLine3" -> "Line 3",
            "addressLine4" -> "Line 4",
            "postcode" -> "AB111AA"
          ),
          tags = Map(
            "transactionName" -> "Agent ePAYE registration created",
            "path" -> requestPath
          )
        )
      }

      "respond HTTP 200 with a the new unique PAYE code in the response body and audit without optional fields" in {
        givenAuditConnector()
        val result = postRegistration(validPostData)
        val requestPath: String = s"/agent-epaye-registration/registrations"

        result.status shouldBe 200
        result.json shouldBe Json.obj("payeAgentReference" -> "HX2000")

        verifyAuditRequestSent(1,
          event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated,
          detail = Map(
            "payeAgentRef" -> "HX2000",
            "agentName" -> "Jim Jiminy",
            "contactName" -> "John Johnson",
            "addressLine1" -> "Line 1",
            "addressLine2" -> "Line 2",
            "postcode" -> "AB111AA"
          ),
          tags = Map(
            "transactionName" -> "Agent ePAYE registration created",
            "path" -> requestPath
          )
        )
      }
    }

    "POST /registrations with invalid details" should {
      "respond with HTTP 400 Bad Request" when {

        "no details are given" in {
          givenAuditConnector()
          val result = postRegistration(Json.obj())
          result.status shouldBe 400

          verifyAuditRequestNotSent(event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated)
        }

        "some mandatory field was missing" in {
          givenAuditConnector()
          val postDataMissingField = validPostData - "agentName"

          val result = postRegistration(postDataMissingField)

          result.status shouldBe 400

          verifyAuditRequestNotSent(event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated)
        }

        "some field was invalid" in {
          givenAuditConnector()
          val postDataInvalidField = validPostData + ("agentName" -> JsString("Invalid#Name"))

          val result = postRegistration(postDataInvalidField)

          result.status shouldBe 400

          verifyAuditRequestNotSent(event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated)
        }
      }
    }

    "GET /registrations" should {

      "respond 200 OK" when {
        "request is authorised by expected stride enrolment" in {
          givenAuditConnector()
          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication", "StrideUserId")

          val result = getRegistrations("2001-01-01", "2001-01-01")
          result.status shouldBe 204

          verifyAuditRequestSentWithExtractDate(1)
          verifyAuditRequestSent(1,
            event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationExtract,
            detail = Map(
              "strideUserId" -> "StrideUserId",
              "dateFrom" -> "2001-01-01",
              "dateTo" -> "2001-01-01",
              "recordCount" -> "0"
            ),
            tags = Map(
              "transactionName" -> "agent-epaye-registration-extract",
              "path" -> "/agent-epaye-registration/registrations"
            )
          )
        }
      }

      "respond 200 OK with registrations in JSON" when {
        "request contains valid parameters and registrations are present in repository" in {
          val creationTime = DateTime.now(DateTimeZone.UTC).minusDays(1)
          val repo = app.injector.instanceOf[AgentEpayeRegistrationRepository]

          val regReq = RegistrationRequest("", "", None, None, None, Address("", "", None, None, ""))
          import scala.concurrent.ExecutionContext.Implicits.global
          await(repo.create(regReq, creationTime))

          givenAuditConnector()
          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication", "StrideUserId")
          val dateParam = creationTime.toLocalDate.toString
          val result = getRegistrations(dateParam, dateParam)
          result.status shouldBe 200

          val responseJson = Json.parse(result.body)
          (responseJson \ "complete").toOption.get shouldBe JsBoolean(true)

          val reg = responseJson \ "registrations" \ 0
          (reg \ "agentReference").toOption.get shouldBe JsString("HX2000")
          (reg \ "createdDateTime").toOption.get shouldBe JsString(creationTime.toString)

          verifyAuditRequestSentWithExtractDate(1)
          verifyAuditRequestSent(1,
            event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationExtract,
            detail = Map(
              "strideUserId" -> "StrideUserId",
              "dateFrom" -> dateParam,
              "dateTo" -> dateParam,
              "recordCount" -> "1"
            ),
            tags = Map(
              "transactionName" -> "agent-epaye-registration-extract",
              "path" -> "/agent-epaye-registration/registrations"
            )
          )
        }
      }

      "respond 403 Forbidden" when {
        "request is authenticated but not with expected stride enrolment" in {
          givenAuthorisedFor("OtherStrideEnrolment", "PrivilegedApplication", "StrideUserId")
          val result = getRegistrations("2001-01-01", "2001-01-01")
          result.status shouldBe 403
          verifyAuditRequestNotSent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationExtract)
        }
      }

      "respond 401 Unauthorised" when {
        "request is not authorised for a MDTP detail - NoActiveSession" in {
          givenRequestIsNotAuthorised("MissingBearerToken")
          val result = getRegistrations("2001-01-01", "2001-01-01")
          result.status shouldBe 401
          verifyAuditRequestNotSent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationExtract)
        }
      }

      "respond 400 Bad Request" when {
        "date range parameters are not in ISO date format" in {
          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication", "StrideUserId")
          val result = getRegistrations("01-01-2001", "01-01-2001")
          result.status shouldBe 400
          result.header("Content-Type") shouldBe Some("application/json")

          result.body should include("'From' date must be in ISO format (yyyy-MM-dd)")
          verifyAuditRequestNotSent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationExtract)
        }

        "date validation fails" in {
          givenAuthorisedFor("ValidStrideEnrolment", "PrivilegedApplication", "StrideUserId")
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
          verifyAuditRequestNotSent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationExtract)
        }
      }
    }
  }
}
