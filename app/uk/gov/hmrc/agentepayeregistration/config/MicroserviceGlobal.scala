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

package uk.gov.hmrc.agentepayeregistration.config

import akka.actor.ActorSystem
import com.typesafe.config.Config
import javax.inject.{Inject, Named, Singleton}
import play.api.Play
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.scheduling.{RunningOfScheduledJobs, ScheduledJob}

object MicroserviceGlobal extends RunningOfScheduledJobs {

  override lazy val scheduledJobs = Play.current.injector.instanceOf[Jobs].lookupJobs()

  @Singleton
  class HttpVerbs @Inject()(val actorSystem: ActorSystem, val auditConnector: AuditConnector, @Named("appName") val appName: String)
    extends HttpGet with HttpPost with HttpPut with HttpPatch with HttpDelete with WSHttp with HttpAuditing {
    override val hooks = Seq(AuditingHook)

    override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
  }


  trait JobsList {
    def lookupJobs(): Seq[ScheduledJob] = Seq()
  }

  @Singleton
  class Jobs @Inject()(
                        @Named("remove-stale-reference-fields-jobs") removeStaleFieldsJob: ScheduledJob
                      ) extends JobsList {
    override def lookupJobs(): Seq[ScheduledJob] =
      Seq(
        removeStaleFieldsJob
      )
  }

}
