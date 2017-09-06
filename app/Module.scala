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

import java.net.{InetSocketAddress, URL}
import java.util.concurrent.TimeUnit.{MILLISECONDS, SECONDS}
import javax.inject.{Inject, Provider, Singleton}

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import com.codahale.metrics.{MetricFilter, SharedMetricRegistries}
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.slf4j.MDC
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment, Logger, Mode}
import uk.gov.hmrc.play.http.{HttpGet, HttpPost}
import wiring.WSVerbs

import scala.concurrent.{ExecutionContext, Future}

class Module(val environment: Environment, val configuration: Configuration) extends AbstractModule with ServicesConfig {

  def configure(): Unit = {
    lazy val appName = configuration.getString("appName").get
    lazy val loggerDateFormat: Option[String] = configuration.getString("logger.json.dateformat")

    Logger.info(s"Starting microservice : $appName : in mode : ${environment.mode}")
    MDC.put("appName", appName)
    loggerDateFormat.foreach(str => MDC.put("logger.json.dateformat", str))

    bind(classOf[HttpGet]).toInstance(new WSVerbs)
    bind(classOf[HttpPost]).toInstance(new WSVerbs)
    bindBaseUrl("auth")
    bindProperty("extract.auth.stride.enrolment")
    bind(classOf[GraphiteStartUp]).asEagerSingleton()
  }

  private def bindBaseUrl(serviceName: String) =
    bind(classOf[URL]).annotatedWith(Names.named(s"$serviceName-baseUrl")).toProvider(new BaseUrlProvider(serviceName))

  private class BaseUrlProvider(serviceName: String) extends Provider[URL] {
    override lazy val get = new URL(baseUrl(serviceName))
  }

  private def bindProperty(propertyName: String) =
    bind(classOf[String]).annotatedWith(Names.named(propertyName)).toProvider(new PropertyProvider(propertyName))

  private class PropertyProvider(confKey: String) extends Provider[String] {
    override lazy val get = configuration.getString(confKey)
      .getOrElse(throw new IllegalStateException(s"No value found for configuration property $confKey"))
  }
}

@Singleton
class GraphiteStartUp @Inject()(configuration: Configuration,
                                lifecycle: ApplicationLifecycle,
                                implicit val ec: ExecutionContext) {

  val metricsPluginEnabled: Boolean = configuration.getBoolean("metrics.enabled").getOrElse(false)

  val graphitePublisherEnabled: Boolean = configuration.getBoolean("microservice.metrics.graphite.enabled").getOrElse(false)

  val graphiteEnabled: Boolean = metricsPluginEnabled && graphitePublisherEnabled

  val registryName: String = configuration.getString("metrics.name").getOrElse("default")

  val graphite = new Graphite(new InetSocketAddress(
    configuration.getString("graphite.host").getOrElse("graphite"),
    configuration.getInt("graphite.port").getOrElse(2003)))

  val prefix: String = configuration.getString("graphite.prefix").
    getOrElse(s"tax.${configuration.getString("appName")}")

  val reporter: GraphiteReporter = GraphiteReporter.forRegistry(
    SharedMetricRegistries.getOrCreate(registryName))
    .prefixedWith(s"$prefix.${java.net.InetAddress.getLocalHost.getHostName}")
    .convertRatesTo(SECONDS)
    .convertDurationsTo(MILLISECONDS)
    .filter(MetricFilter.ALL)
    .build(graphite)

  private def startGraphite() {
    Logger.info("Graphite metrics enabled, starting the reporter")
    reporter.start(configuration.getLong("graphite.interval").getOrElse(10L), SECONDS)
  }

  if (graphiteEnabled) startGraphite()
  lifecycle.addStopHook { () =>
    Future successful reporter.stop()
  }
}

trait ServicesConfig {

  def environment: Environment
  def configuration: Configuration

  lazy val env = if (environment.mode.equals(Mode.Test)) "Test" else configuration.getString("run.mode").getOrElse("Dev")
  private lazy val rootServices = "microservice.services"
  private lazy val services = s"$env.microservice.services"
  private lazy val playServices = s"govuk-tax.$env.services"

  private lazy val defaultProtocol: String =
    configuration.getString(s"$rootServices.protocol")
      .getOrElse(configuration.getString(s"$services.protocol")
        .getOrElse("http"))

  def baseUrl(serviceName: String) = {
    val protocol = getConfString(s"$serviceName.protocol",defaultProtocol)
    val host = getConfString(s"$serviceName.host", throw new RuntimeException(s"Could not find config $serviceName.host"))
    val port = getConfInt(s"$serviceName.port", throw new RuntimeException(s"Could not find config $serviceName.port"))
    s"$protocol://$host:$port"
  }

  private def getConfString(confKey: String, defString: => String) = {
    configuration.getString(s"$rootServices.$confKey").
      getOrElse(configuration.getString(s"$services.$confKey").
        getOrElse(configuration.getString(s"$playServices.$confKey").
          getOrElse(defString)))
  }

  private def getConfInt(confKey: String, defInt: => Int) = {
    configuration.getInt(s"$rootServices.$confKey").
      getOrElse(configuration.getInt(s"$services.$confKey").
        getOrElse(configuration.getInt(s"$playServices.$confKey").
          getOrElse(defInt)))
  }

}
