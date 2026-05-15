import sbt.*

object AppDependencies {
  lazy val hmrcMongoVersion = "2.12.0"
  lazy val bootstrapVersion = "10.7.0"

  lazy val compileDeps: Seq[ModuleID] = Seq(
    "org.typelevel"     %% "cats-core"                 % "2.13.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.typesafe.play" %% "play-json"                 % "2.10.8",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  def testDeps: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"   %% "mockito-4-11"            % "3.2.18.0",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compileDeps ++ testDeps
}
