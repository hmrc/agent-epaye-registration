package uk.gov.hmrc.agentepayeregistration.controllers

import play.api.Configuration
import play.api.libs.json._
import uk.gov.hmrc.agentepayeregistration.audit.AgentEpayeRegistrationEvent
import uk.gov.hmrc.agentepayeregistration.models.AgentReference
import uk.gov.hmrc.agentepayeregistration.repository.AgentEpayeRegistrationRepository
import uk.gov.hmrc.agentepayeregistration.stubs.{AuthStub, DataStreamStub}
import uk.gov.hmrc.agentepayeregistration.support.RegistrationActions
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class AgentEpayeRegistrationControllerISpec extends BaseControllerISpec with AuthStub with RegistrationActions with DataStreamStub with DefaultPlayMongoRepositorySupport[AgentReference] {

  private lazy val configuration = app.injector.instanceOf[Configuration]
  override lazy val repository = new AgentEpayeRegistrationRepository(mongoComponent, configuration)

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
        givenAgentKnownFactsComplete(AgentReference("HX2000"))

        val result = postRegistration(validPostDataComplete)
        val requestPath: String = s"/agent-epaye-registration/registrations"

        result.status mustBe 200

        result.json mustBe Json.obj("payeAgentReference" -> "HX2000")

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
        givenAgentKnownFactsIncomplete(AgentReference("HX2000"))

        val result = postRegistration(validPostData)
        val requestPath: String = s"/agent-epaye-registration/registrations"

        result.status mustBe 200
        result.json mustBe Json.obj("payeAgentReference" -> "HX2000")

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

    "POST /registrations returns error message if DES is unavailable" in {
      createAgentKnownFactsFailsWithStatus(AgentReference("HX2000"), 500)
      val result = postRegistration(validPostDataComplete)
      result.status mustBe 502
    }

    "POST /registrations with invalid details" should {
      "respond with HTTP 400 Bad Request" when {

        "no details are given" in {
          givenAuditConnector()
          val result = postRegistration(Json.obj())
          result.status mustBe 400

          verifyAuditRequestNotSent(event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated)
        }

        "some mandatory field was missing" in {
          givenAuditConnector()
          val postDataMissingField = validPostData - "agentName"

          val result = postRegistration(postDataMissingField)

          result.status mustBe 400

          verifyAuditRequestNotSent(event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated)
        }

        "some field was invalid" in {
          givenAuditConnector()
          val postDataInvalidField = validPostData + ("agentName" -> JsString("Invalid#Name"))

          val result = postRegistration(postDataInvalidField)

          result.status mustBe 400

          verifyAuditRequestNotSent(event = AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated)
        }
      }
    }
  }
}
