import uk.gov.hmrc.DefaultBuildSettings.itSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;ErrorHandler;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum    := false,
    ScoverageKeys.coverageHighlighting     := true,
    Test / parallelExecution               := false
  )
}

lazy val root = (project in file("."))
  .settings(
    name                     := "agent-epaye-registration",
    organization             := "uk.gov.hmrc",
    isPublicArtefact         := true,
    PlayKeys.playDefaultPort := 9445,
    libraryDependencies ++= AppDependencies(),
    routesImport += "uk.gov.hmrc.agentepayeregistration.controllers.UrlBinders._",
    scoverageSettings,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:cat=unused&src=routes/.*:s",
      "-Wconf:cat=unused&src=views/.*:s"
    )
  )
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin): _*)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test")
  .settings(itSettings(): _*)
