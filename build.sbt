import sbt.Tests.Group
import uk.gov.hmrc.ForkedJvmPerTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val compileDeps = Seq(
  "uk.gov.hmrc"       %% "bootstrap-backend-play-28"  % "5.24.0",
  "org.typelevel"     %% "cats"                       % "0.9.0",
  "uk.gov.hmrc"       %% "agent-kenshoo-monitoring"   % "4.8.0-play-28",
  "uk.gov.hmrc"       %% "mongo-lock"                 % "7.1.0-play-28",
  "uk.gov.hmrc"       %% "emailaddress"               % "3.6.0",
  "com.typesafe.play" %% "play-json"                  % "2.9.2",
  "com.typesafe.akka" %% "akka-protobuf"              % "2.6.19"
)

def testDeps(scope: String) = Seq(
  "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.25.0"          % scope,
  "org.mockito"              % "mockito-core"               % "4.6.1"          % scope,
  "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2"        % scope,
  "com.github.tomakehurst"   % "wiremock-standalone"        % "2.27.2"          % scope,
  "org.pegdown"              % "pegdown"                    % "1.6.0"           % scope

)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;ErrorHandler;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val root = (project in file("."))
  .settings(
    name := "agent-epaye-registration",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.12",
    majorVersion := 0,
    isPublicArtefact := true,
    PlayKeys.playDefaultPort := 9445,
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    routesImport += "uk.gov.hmrc.agentepayeregistration.controllers.UrlBinders._",
    publishingSettings,
    scoverageSettings,
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.full
    ),
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value)
  )
  .configs(IntegrationTest)
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) : _*)



def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = ForkedJvmPerTestSettings.oneForkedJvmPerTest(tests)
