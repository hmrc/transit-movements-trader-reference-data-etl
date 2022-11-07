import play.core.PlayVersion._
import sbt._

object AppDependencies {

  private val catsVersion = "2.8.0"
  private val mongoVersion = "0.73.0"
  private val bootstrapVersion = "7.11.0"
  private val akkaVersion = "2.6.20"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28"          % bootstrapVersion,
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-28"                 % mongoVersion,
    "org.typelevel"      %% "cats-core"                          % catsVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % "3.0.4",
    "com.enragedginger"  %% "akka-quartz-scheduler"              % "1.9.0-akka-2.6.x"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % mongoVersion,
    "org.scalatest"          %% "scalatest"                % "3.2.14",
    "com.typesafe.play"      %% "play-test"                % current,
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.mockito"             % "mockito-core"             % "4.8.0",
    "org.scalatestplus"      %% "mockito-4-6"              % "3.2.14.0",
    "org.scalacheck"         %% "scalacheck"               % "1.17.0",
    "org.scalatestplus"      %% "scalacheck-1-17"          % "3.2.14.0",
    "com.typesafe.akka"      %% "akka-stream-testkit"      % akkaVersion,
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.62.2",
    "com.github.tomakehurst"  % "wiremock-standalone"      % "2.27.2"
  ).map(_ % "test, it")
}
