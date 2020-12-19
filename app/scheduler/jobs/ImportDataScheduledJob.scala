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

package scheduler.jobs

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import scheduler.SchedulingActor.ImportDataMessage
import scheduler.config.ScheduledJobConfig
import scheduler.tasks.ImportDataTask

class ImportDataScheduledJob @Inject() (
  config: Configuration,
  val applicationLifecycle: ApplicationLifecycle,
  task: ImportDataTask
) extends ScheduledJob {

  override val scheduledMessage: ImportDataMessage = ImportDataMessage(task)
  override val jobName: JobName                    = JobName.ImportData
  override val actorSystem: ActorSystem            = ActorSystem(jobName)
  override val settings: ScheduledJobConfig        = config.get[ScheduledJobConfig](s"schedules.$jobName")

  setUpSchedule()
}
