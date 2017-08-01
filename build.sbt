import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val testSettings = Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % "test, it",
  "org.mockito" % "mockito-core" % "2.8.9" % "test, it",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" %  "test, it"
)

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
    libraryDependencies ++= Seq(
      "uk.gov.hmrc" %% "http-verbs" % "6.4.0",
      "uk.gov.hmrc" %% "play-auditing" % "2.10.0",
      "uk.gov.hmrc" %% "play-auth" % "1.1.0",
      "uk.gov.hmrc" %% "play-config" % "4.3.0",
      "uk.gov.hmrc" %% "play-graphite" % "3.2.0",
      "uk.gov.hmrc" %% "play-health" % "2.1.0",
      "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
      "de.threedimensions" %% "metrics-play" % "2.5.13"
    ) ++ testSettings,
    publishingSettings
  )
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    libraryDependencies ++= testSettings,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value)
  )
  .enablePlugins(PlayScala, SbtGitVersioning, SbtDistributablesPlugin)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq(s"-Dtest.name=${test.name}"))))
  }
}