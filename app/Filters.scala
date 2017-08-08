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

import javax.inject.Inject

import akka.stream.Materializer
import com.kenshoo.play.metrics.MetricsFilter
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.http.DefaultHttpFilters
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.filter.{AuthorisationFilter, FilterConfig}
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.inject.{DefaultRunMode, DefaultServicesConfig, RunMode}
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.ExecutionContext

/**
  * Defines the filters that are added to the application by extending the default Play filters
  *
  * @param loggingFilter - used to log details of any http requests hitting the service
  * @param auditFilter   - used to call the datastream microservice and publish auditing events
  * @param metricsFilter - used to collect metrics and statistics relating to the service
  * @param authFilter    - used to add authorisation to endpoints in the service if required
  */
class Filters @Inject()(loggingFilter: LogFilter, auditFilter: MicroserviceAuditFilter, metricsFilter: MetricsFilter,
                        authFilter: MicroserviceAuthFilter)
  extends DefaultHttpFilters(loggingFilter, auditFilter, metricsFilter, authFilter)

class LogFilter @Inject()(implicit val mat: Materializer, configuration: Configuration) extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String): Boolean = configuration.getBoolean(s"controllers.$controllerName.needsLogging").getOrElse(true)
}

class MicroserviceAuditFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext,
                                        configuration: Configuration, val auditConnector: MicroserviceAuditConnector) extends AuditFilter {

  override def controllerNeedsAuditing(controllerName: String): Boolean = configuration.getBoolean(s"controllers.$controllerName.needsAuditing").getOrElse(true)

  override def appName: String = configuration.getString("appName").get
}

class MicroserviceAuditConnector @Inject()(val environment: Environment) extends AuditConnector {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

class MicroserviceAuthFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext,
                                       configuration: Configuration, val connector: AuthConn) extends AuthorisationFilter {
  override def config: FilterConfig = FilterConfig(configuration.underlying.as[Config]("controllers"))
}

class AuthConn @Inject()(defaultServicesConfig: DefaultServicesConfig,
                         val http: WsVerbs) extends PlayAuthConnector {

  override val serviceUrl: String = defaultServicesConfig.baseUrl("auth")
}

class WsVerbs extends WSHttp {
  override val hooks = NoneRequired
}
