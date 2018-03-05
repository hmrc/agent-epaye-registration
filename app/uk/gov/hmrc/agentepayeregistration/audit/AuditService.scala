/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentepayeregistration.audit

import javax.inject.Inject

import com.google.inject.Singleton
import play.api.mvc.Request
import uk.gov.hmrc.agentepayeregistration.audit.AgentEpayeRegistrationEvent.AgentEpayeRegistrationEvent
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, RegistrationDetails, RegistrationRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._


import scala.concurrent.Future
import scala.util.Try

object AgentEpayeRegistrationEvent extends Enumeration {
  val AgentEpayeRegistrationRecordCreated = Value
  val AgentEpayeRegistrationExtract = Value
  type AgentEpayeRegistrationEvent = Value
}

@Singleton
class AuditService @Inject()(val auditConnector: AuditConnector) {

  def sendAgentEpayeRegistrationRecordCreated(registrationRequest: RegistrationRequest, agentReference: AgentReference)(implicit hc: HeaderCarrier, request: Request[Any]): Unit = {

    auditEvent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated, "Agent ePAYE registration created",
      Seq("payeAgentRef" -> agentReference.value,
        "agentName" -> registrationRequest.agentName,
        "contactName" -> registrationRequest.contactName,
        "phoneNo" -> registrationRequest.phoneNo.getOrElse(""),
        "faxNumber" -> registrationRequest.faxNumber.getOrElse(""),
        "email" -> registrationRequest.email.getOrElse(""),
        "addressLine1" -> s"${registrationRequest.address.addressLine1}",
        "addressLine2" -> s"${registrationRequest.address.addressLine2}",
        "addressLine3" -> s"${registrationRequest.address.addressLine3.getOrElse("")}",
        "addressLine4" -> s"${registrationRequest.address.addressLine4.getOrElse("")}",
        "postcode" -> s"${registrationRequest.address.postCode}").filter(_._2 != ""))
  }

  def sendAgentKnownFactsCreated(registrationDetails: RegistrationDetails)(implicit hc: HeaderCarrier, request: Request[Any]): Unit = {

    auditEvent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated, "Agent ePAYE registration created",
      Seq("payeAgentRef" -> registrationDetails.agentReference.value,
        "agentName" -> registrationDetails.registration.agentName,
        "contactName" -> registrationDetails.registration.contactName,
        "phoneNo" -> registrationDetails.registration.phoneNo.getOrElse(""),
        "faxNumber" -> registrationDetails.registration.faxNumber.getOrElse(""),
        "email" -> registrationDetails.registration.email.getOrElse(""),
        "addressLine1" -> s"${registrationDetails.registration.address.addressLine1}",
        "addressLine2" -> s"${registrationDetails.registration.address.addressLine2}",
        "addressLine3" -> s"${registrationDetails.registration.address.addressLine3.getOrElse("")}",
        "addressLine4" -> s"${registrationDetails.registration.address.addressLine4.getOrElse("")}",
        "postcode" -> s"${registrationDetails.registration.address.postCode}",
        "createdDate" -> s"${registrationDetails.createdDateTime.toDate.toString}").filter(_._2 != ""))
  }

  def sendAgentEpayeRegistrationExtract(userId: String, extractDate: String, dataFrom: String, dateTo: String, count: Int)(implicit hc: HeaderCarrier, request: Request[Any]): Unit  = {

      auditEvent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationExtract, "agent-epaye-registration-extract",
        Seq(
          "strideUserId" -> userId,
          "extractDate" -> extractDate,
          "dateFrom" -> dataFrom,
          "dateTo" -> dateTo,
          "recordCount" -> count
        )
      )
  }

  private[audit] def auditEvent(event: AgentEpayeRegistrationEvent, transactionName: String, details: Seq[(String, Any)] = Seq.empty)
                               (implicit hc: HeaderCarrier, request: Request[Any]): Future[Unit] = {
    send(createEvent(event, transactionName, details: _*))
  }

  private def createEvent(event: AgentEpayeRegistrationEvent, transactionName: String, details: (String, Any)*)
                         (implicit hc: HeaderCarrier, request: Request[Any]): DataEvent = {

    val detail = hc.toAuditDetails(details.map(pair => pair._1 -> pair._2.toString): _*)
    val tags = hc.toAuditTags(transactionName, request.path)
    DataEvent(auditSource = "agent-epaye-registration",
      auditType = event.toString,
      tags = tags,
      detail = detail
    )
  }

  private def send(events: DataEvent*)(implicit hc: HeaderCarrier): Future[Unit] = {
    Future {
      events.foreach { event =>
        Try(auditConnector.sendEvent(event))
      }
    }
  }

}
