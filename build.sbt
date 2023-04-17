import uk.gov.hmrc.DefaultBuildSettings.oneForkedJvmPerTest

val hmrcMongoVersion = "0.74.0"

lazy val compileDeps = Seq(
  "org.typelevel"     %% "cats-core"                  % "2.9.0",
  "uk.gov.hmrc"       %% "agent-kenshoo-monitoring"   % "5.3.0",
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
  "uk.gov.hmrc"       %% "emailaddress"               % "3.8.0",
  "com.typesafe.play" %% "play-json"                  % "2.9.2",
  "joda-time"         % "joda-time"                   % "2.12.1"
)

def testDeps(scope: String) = Seq(
  "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "7.15.0"          % scope,
  "org.mockito"              % "mockito-core"               % "4.6.1"           % scope,
  "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion          % scope,
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
    Test / parallelExecution := false
  )
}

lazy val root = (project in file("."))
  .settings(
    name := "agent-epaye-registration",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.13.8",
    majorVersion := 0,
    isPublicArtefact := true,
    PlayKeys.playDefaultPort := 9445,
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    routesImport += "uk.gov.hmrc.agentepayeregistration.controllers.UrlBinders._",
    scoverageSettings,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:cat=unused&src=routes/.*:s",
      "-Wconf:cat=unused&src=views/.*:s",
    ),
    IntegrationTest / Keys.fork := false,
    Defaults.itSettings,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value)
  )
  .configs(IntegrationTest)
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) : _*)

