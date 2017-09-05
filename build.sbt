import play.core.PlayVersion
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val compileDeps = Seq(
  "uk.gov.hmrc" %% "http-verbs" % "6.4.0",
  "uk.gov.hmrc" %% "play-auditing" % "2.10.0",
  "uk.gov.hmrc" %% "play-auth" % "2.2.1",
  "uk.gov.hmrc" %% "play-config" % "4.3.0",
  "uk.gov.hmrc" %% "play-graphite" % "3.2.0",
  "uk.gov.hmrc" %% "play-health" % "2.1.0",
  "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
  "de.threedimensions" %% "metrics-play" % "2.5.13",
  "uk.gov.hmrc" %% "play-reactivemongo" % "5.2.0",
  "uk.gov.hmrc" %% "microservice-bootstrap" % "5.16.0",
  "org.typelevel" %% "cats" % "0.9.0"
)

def testDeps(scope: String) = Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % scope,
  "org.mockito" % "mockito-core" % "2.8.9" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % scope,
  "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.3.1" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope
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
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.bintrayRepo("hmrc", "release-candidates"),
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
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
  .enablePlugins(Seq(PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) : _*)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq(s"-Dtest.name=${test.name}"))))
  }
}