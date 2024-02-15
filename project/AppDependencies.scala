import play.core.PlayVersion._
import sbt._

object AppDependencies {

  private val catsVersion = "2.9.0"
  private val mongoVersion = "1.6.0"
  private val bootstrapVersion = "8.4.0"
  private val akkaVersion = "2.6.21"
  private val pekkoVersion = "1.0.2"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-backend-play-30"          % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-play-30"                 % mongoVersion,
    "org.typelevel"          %% "cats-core"                          % catsVersion,
    "com.lightbend.akka"     %% "akka-stream-alpakka-json-streaming" % "3.0.4",
    "com.enragedginger"      %% "akka-quartz-scheduler"              % "1.9.0-akka-2.6.x",
    "io.github.samueleresca" %% "pekko-quartz-scheduler"             % "1.1.0-pekko-1.0.x",
    "org.apache.pekko"       %% "pekko-connectors-json-streaming"    % pekkoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatest"          %% "scalatest"                % "3.2.15",
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"             % "mockito-core"             % "5.2.0",
    "org.scalatestplus"      %% "mockito-4-6"              % "3.2.15.0",
    "org.scalacheck"         %% "scalacheck"               % "1.17.0",
    "org.scalatestplus"      %% "scalacheck-1-17"          % "3.2.16.0",
    "com.typesafe.akka"      %% "akka-stream-testkit"      % akkaVersion,
    "org.apache.pekko"       %% "pekko-testkit"            % pekkoVersion,
    "org.apache.pekko"       %% "pekko-stream-testkit"     % pekkoVersion
  ).map(_ % "test, it")
}
