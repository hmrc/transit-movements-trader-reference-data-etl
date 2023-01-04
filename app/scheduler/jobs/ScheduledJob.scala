/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scheduler.jobs

import akka.actor.ActorRef
import akka.actor.ActorSystem
import com.typesafe.akka.`extension`.quartz.QuartzSchedulerExtension
import logging.Logging

import play.api.inject.ApplicationLifecycle
import scheduler.config.ScheduledJobConfig
import scheduler.jobs.SchedulingActor.ScheduledMessage

import scala.concurrent.Future

private[scheduler] trait ScheduledJob extends Logging {

  val scheduledMessage: ScheduledMessage[_]
  val actorSystem: ActorSystem
  val jobName: JobName
  val applicationLifecycle: ApplicationLifecycle

  lazy val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  lazy val schedulingActorRef: ActorRef = actorSystem.actorOf(SchedulingActor.props)

  val settings: ScheduledJobConfig

  def setUpSchedule(): Unit =
    if (settings.enabled) {
      logger.info(s"Scheduling $jobName with schedule ${settings.expression}")

      scheduler.createSchedule(
        name = jobName,
        description = settings.description,
        cronExpression = settings.expression
      )

      scheduler.schedule(jobName, schedulingActorRef, scheduledMessage)
    } else
      logger.info(s"$jobName is disabled in configuration")

  applicationLifecycle.addStopHook {
    () =>
      Future.successful(scheduler.cancelJob(jobName))
      Future.successful(scheduler.shutdown(waitForJobsToComplete = false))
  }
}
