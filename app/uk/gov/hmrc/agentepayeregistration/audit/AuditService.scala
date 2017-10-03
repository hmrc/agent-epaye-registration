/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.agentepayeregistration.models.{AgentReference, RegistrationRequest}
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future
import scala.util.Try

object AgentEpayeRegistrationEvent extends Enumeration {
  val AgentEpayeRegistrationRecordCreated = Value
  type AgentEpayeRegistrationEvent = Value
}

@Singleton
class AuditService @Inject()(val auditConnector: AuditConnector) {

  def sendAgentEpayeRegistrationRecordCreated(registrationRequest: RegistrationRequest, agentReference: AgentReference)(implicit hc: HeaderCarrier, request: Request[Any]): Unit = {

    auditEvent(AgentEpayeRegistrationEvent.AgentEpayeRegistrationRecordCreated, "agent-epaye-registration-record-created",
      Seq("agentReference" -> agentReference.value,
        "agentName" -> registrationRequest.agentName,
        "contactName" -> registrationRequest.contactName,
        "telephoneNumber" -> registrationRequest.telephoneNumber.getOrElse(""),
        "faxNumber" -> registrationRequest.faxNumber.getOrElse(""),
        "emailAddress" -> registrationRequest.emailAddress.getOrElse(""),
        "address" -> s"${registrationRequest.address}"))
  }

  private[audit] def auditEvent(event: AgentEpayeRegistrationEvent, transactionName: String, details: Seq[(String, Any)] = Seq.empty)
                               (implicit hc: HeaderCarrier, request: Request[Any]): Future[Unit] = {
    send(createEvent(event, transactionName, details: _*))
  }

  private def createEvent(event: AgentEpayeRegistrationEvent, transactionName: String, details: (String, Any)*)
                         (implicit hc: HeaderCarrier, request: Request[Any]): DataEvent = {

    def toString(x: Any): String = x match {
      case _ => x.toString
    }

    val detail = hc.toAuditDetails(details.map(pair => pair._1 -> toString(pair._2)): _*)
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
