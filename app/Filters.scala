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
import play.api.Configuration
import play.api.http.DefaultHttpFilters
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.http.{HttpDelete, HttpGet, HttpPost, HttpPut}
import uk.gov.hmrc.play.config.inject.{AppName, DefaultServicesConfig}
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter}

import scala.concurrent.ExecutionContext

/**
  * Defines the filters that are added to the application by extending the default Play filters
  *
  * @param loggingFilter - used to log details of any http requests hitting the service
  * @param auditFilter   - used to call the datastream microservice and publish auditing events
  * @param metricsFilter - used to collect metrics and statistics relating to the service
  */
class Filters @Inject()(loggingFilter: LogFilter, auditFilter: MicroserviceAuditFilter, metricsFilter: MetricsFilter)
  extends DefaultHttpFilters(loggingFilter, auditFilter, metricsFilter)

class LogFilter @Inject()(implicit val mat: Materializer, configuration: Configuration) extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String): Boolean = configuration.getBoolean(s"controllers.$controllerName.needsLogging").getOrElse(true)
}

class MicroserviceAuditFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext,
                                        configuration: Configuration, val auditConnector: MicroserviceAuditConnector) extends AuditFilter with AppName {

  override def controllerNeedsAuditing(controllerName: String): Boolean = configuration.getBoolean(s"controllers.$controllerName.needsAuditing").getOrElse(true)

  override def appName: String = configuration.getString("appName").get

  override protected def appNameConfiguration: Configuration = configuration
}

class AuthConn @Inject()(defaultServicesConfig: DefaultServicesConfig,
                         val http: WSHttp) extends PlayAuthConnector {

  override val serviceUrl: String = defaultServicesConfig.baseUrl("auth")
}

trait Hooks extends HttpHooks {
  override val hooks = NoneRequired
}

class WSHttp @Inject()(implicit configuration: Configuration) extends HttpGet with WSGet with HttpPut
  with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks
