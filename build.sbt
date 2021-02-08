import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.ForkedJvmPerTestSettings

lazy val compileDeps = Seq(
  "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "3.4.0",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.31.0-play-27",
  "org.typelevel" %% "cats" % "0.9.0",
  "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "4.4.0",
  "uk.gov.hmrc" %% "mongo-lock" % "6.24.0-play-27",
  "uk.gov.hmrc" %% "emailaddress" % "3.5.0",
  "com.typesafe.play" %% "play-json" % "2.9.1"
)

def testDeps(scope: String) = Seq(
  "org.scalatest" %% "scalatest" % "3.0.9" % scope,
  "org.mockito" % "mockito-core" % "3.6.28" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.27.2" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.22.0-play-27" % scope
)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;ErrorHandler;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimum := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}
val jettyVersion = "9.2.24.v20180105"
lazy val root = (project in file("."))
  .settings(
    name := "agent-epaye-registration",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.12",
    PlayKeys.playDefaultPort := 9445,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.bintrayRepo("hmrc", "release-candidates"),
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    dependencyOverrides ++= Seq(
      "org.eclipse.jetty" % "jetty-server" % jettyVersion % "it",
      "commons-codec" % "commons-codec" % "1.12",
      "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-security" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-continuation" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-xml" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-client" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-http" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-io" % jettyVersion % "it",
      "org.eclipse.jetty" % "jetty-util" % jettyVersion % "it",
      "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion % "it",
      "org.eclipse.jetty.websocket" % "websocket-common" % jettyVersion % "it",
      "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion % "it"
    ),
    routesImport += "uk.gov.hmrc.agentepayeregistration.controllers.UrlBinders._",
    publishingSettings,
    scoverageSettings,
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.full
    ),
  )
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value)
  )
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) : _*)
  .settings(majorVersion := 0)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = ForkedJvmPerTestSettings.oneForkedJvmPerTest(tests)
