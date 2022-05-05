import play.core.PlayVersion
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val catsVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28"          % "5.24.0",
    "org.reactivemongo"  %% "play2-reactivemongo"                % "0.20.13-play28",
    "com.typesafe.play"  %% "play-iteratees"                     % "2.6.1",
    "com.typesafe.play"  %% "play-iteratees-reactive-streams"    % "2.6.1",
    "org.typelevel"      %% "cats-core"                          % catsVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % "3.0.4",
    "com.enragedginger"  %% "akka-quartz-scheduler"              % "1.9.0-akka-2.6.x"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                % "3.2.9",
    "com.typesafe.play"      %% "play-test"                % current,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"      %% "scalatestplus-mockito"    % "1.0.0-M2",
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.9.0",
    "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "com.typesafe.akka"      %% "akka-stream-testkit"      % PlayVersion.akkaVersion,
    "com.typesafe.akka"      %% "akka-slf4j"               % PlayVersion.akkaVersion,
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.35.10",
    "com.github.tomakehurst" % "wiremock-standalone"       % "2.27.2"
  ).map(_ % "test, it")
}
