/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import logging.Logging
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import javax.inject.Inject
import models.TriggerDataImportReqest
import scheduler.SchedulerActions
import models.TriggerDataImportReqest.TriggerAllDataImports

class TriggerReferenceDataImportController @Inject() (cc: ControllerComponents, importActions: SchedulerActions)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def scheduleImport: Action[TriggerDataImportReqest] = Action.async(parse.json[TriggerDataImportReqest]) {
    request =>
      request.body match {
        case TriggerAllDataImports =>
          importActions.triggerReferenceDataImport() map {
            case true  => Ok("")
            case false => InternalServerError
          }

      }
  }

}
