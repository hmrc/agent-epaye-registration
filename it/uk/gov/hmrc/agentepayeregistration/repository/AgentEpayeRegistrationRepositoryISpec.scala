package uk.gov.hmrc.agentepayeregistration.repository

import uk.gov.hmrc.agentepayeregistration.controllers.BaseControllerISpec
import uk.gov.hmrc.agentepayeregistration.models.{Address, AgentReference, RegistrationRequest}

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global

class AgentEpayeRegistrationRepositoryISpec extends BaseControllerISpec {
  private lazy val repo = app.injector.instanceOf[AgentEpayeRegistrationRepository]

  val postcode = "AB11 AA11"
  val addressLine1 = "Address Line 1"
  val addressLine2 = "Address Line 2"
  val addressLine3 = Some("Address Line 3")
  val addressLine4 = Some("Address Line 4")
  val regAddress = Address(addressLine1, addressLine2, addressLine3, addressLine4, postcode)

  val agentName = "Agent Name"
  val contactName = "Contact Name"
  val telephoneNumber = Some("0123456789")
  val faxNumber = Some("0123456780")
  val emailAddress = Some("a@b.com")
  val regDetails = RegistrationRequest(agentName, contactName, telephoneNumber, faxNumber, emailAddress, regAddress)

  override def beforeEach() {
    super.beforeEach()
    await(repo.ensureIndexes)
  }

  "AgentEpayeRegistrationRepository" can {

    "create a RegistrationDetails record" in {
      await(repo.find("agentName" -> agentName)) shouldBe List.empty

      val result = await(repo.create(regDetails))

      result shouldBe AgentReference("HX2000")

      val details = await(repo.find("agentName" -> agentName)).head

      details should have(
        'agentReference (AgentReference("HX2000")),
        'registration (regDetails)
      )

      details.createdDateTime should not be empty
    }

    "create a unique Agent PAYE Reference code" in {
      await(repo.create(regDetails))
      await(repo.create(regDetails))
      await(repo.create(regDetails))

      val results = await(repo.find("agentName" -> agentName))

      results.size shouldBe 3

      results.head.agentReference shouldBe AgentReference("HX2000")
      results.drop(1).head.agentReference shouldBe AgentReference("HX2001")
      results.last.agentReference shouldBe AgentReference("HX2002")
    }
  }
}
