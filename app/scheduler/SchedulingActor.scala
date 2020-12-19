/*
 * Copyright 2020 HM Revenue & Customs
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

package scheduler

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import logging.Logging
import scheduler.SchedulingActor.ScheduledMessage
import scheduler.jobs.JobFailed
import scheduler.tasks.ImportDataTask
import scheduler.tasks.ScheduledTask

class SchedulingActor extends Actor with ActorLogging with Logging {

  override def receive: Receive = {
    case message: ScheduledMessage[_] =>
      logger.info(s"Received message: ${message.getClass.getCanonicalName}")
      message.task.run()
  }
}

object SchedulingActor {

  def props: Props = Props[SchedulingActor]

  sealed trait ScheduledMessage[A] {
    val task: ScheduledTask[A]
  }

  case class ImportDataMessage(task: ImportDataTask) extends ScheduledMessage[Either[JobFailed, Option[Boolean]]]
}
