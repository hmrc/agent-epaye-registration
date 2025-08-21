import sbt.*

object AppDependencies {
  lazy val hmrcMongoVersion = "2.7.0"
  lazy val bootstrapVersion = "10.1.0"

  lazy val compileDeps: Seq[ModuleID] = Seq(
    "org.typelevel"     %% "cats-core"                 % "2.9.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.typesafe.play" %% "play-json"                 % "2.9.2",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  def testDeps: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"   %% "scalatestplus-mockito"   % "1.0.0-M2",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compileDeps ++ testDeps
}
