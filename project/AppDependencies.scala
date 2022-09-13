import play.core.PlayVersion
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val catsVersion = "2.8.0"
  private val mongoVersion = "0.71.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28"          % "7.3.0",
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-28"                 % mongoVersion,
    "com.typesafe.play"  %% "play-iteratees"                     % "2.6.1",
    "com.typesafe.play"  %% "play-iteratees-reactive-streams"    % "2.6.1",
    "org.typelevel"      %% "cats-core"                          % catsVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % "3.0.4",
    "com.enragedginger"  %% "akka-quartz-scheduler"              % "1.9.0-akka-2.6.x"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % mongoVersion,
    "org.scalatest"          %% "scalatest"                % "3.2.12",
    "com.typesafe.play"      %% "play-test"                % current,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0",
    "org.mockito"             % "mockito-core"             % "4.8.0",
    "org.scalatestplus"      %% "mockito-4-5"              % "3.2.12.0",
    "org.scalacheck"         %% "scalacheck"               % "1.16.0",
    "org.scalatestplus"      %% "scalacheck-1-16"          % "3.2.12.0",
    "com.typesafe.akka"      %% "akka-stream-testkit"      % PlayVersion.akkaVersion,
    "com.typesafe.akka"      %% "akka-slf4j"               % PlayVersion.akkaVersion,
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.62.2",
    "com.github.tomakehurst"  % "wiremock-standalone"      % "2.27.2"
  ).map(_ % "test, it")
}
