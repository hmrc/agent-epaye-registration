import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val compileDeps = Seq(
  "uk.gov.hmrc" %% "bootstrap-play-26" % "0.36.0",
  "uk.gov.hmrc" %% "auth-client" % "2.19.0-play-26",
  "de.threedimensions" %% "metrics-play" % "2.5.13",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.12.0-play-26",
  "org.typelevel" %% "cats" % "0.9.0",
  "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "3.4.0",
  "uk.gov.hmrc" %% "mongo-lock" % "6.10.0-play-26",
  "com.typesafe.play" %% "play-json" % "2.7.1"
)

def testDeps(scope: String) = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % scope,
  "org.mockito" % "mockito-core" % "2.24.5" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
  "uk.gov.hmrc" %% "hmrctest" % "3.5.0-play-26" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.21.0" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.8.0-play-26" % scope
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
    scalaVersion := "2.11.12",
    PlayKeys.playDefaultPort := 9445,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.bintrayRepo("hmrc", "release-candidates"),
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    dependencyOverrides ++= Set(
      "org.eclipse.jetty" % "jetty-server" % jettyVersion % "it",
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
    scoverageSettings
  )
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value)
  )
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) : _*)
  .settings(majorVersion := 0)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq(s"-Dtest.name=${test.name}"))))
  }
}