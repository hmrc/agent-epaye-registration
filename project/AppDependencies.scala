import sbt.*


object AppDependencies {
  lazy val hmrcMongoVersion = "2.4.0"
  lazy val bootstrapVersion = "9.7.0"

  lazy val compileDeps: Seq[ModuleID] = Seq(
    "org.typelevel"     %% "cats-core"                  % "2.9.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "com.typesafe.play" %% "play-json"                  % "2.9.2",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion
  )

  def testDeps: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
    "org.pegdown"              % "pegdown"                    % "1.6.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compileDeps ++ testDeps
}