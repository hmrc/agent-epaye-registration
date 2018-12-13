import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val compileDeps = Seq(
  "uk.gov.hmrc" %% "bootstrap-play-25" % "4.3.0",
  "uk.gov.hmrc" %% "auth-client" % "2.17.0-play-25",
  "de.threedimensions" %% "metrics-play" % "2.5.13",
  "uk.gov.hmrc" %% "play-reactivemongo" % "6.2.0",
  "org.typelevel" %% "cats" % "0.9.0",
  "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "3.0.1",

  "uk.gov.hmrc" %% "mongo-lock" % "6.4.0-play-25",
  "uk.gov.hmrc" %% "play-scheduling" % "5.4.0"
)

def testDeps(scope: String) = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % scope,
  "org.mockito" % "mockito-core" % "2.13.0" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
  "uk.gov.hmrc" %% "hmrctest" % "3.3.0" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.17.0" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.4.0-play-25" % scope
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

lazy val root = (project in file("."))
  .settings(
    name := "agent-epaye-registration",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.11.11",
    PlayKeys.playDefaultPort := 9445,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.bintrayRepo("hmrc", "release-candidates"),
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
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