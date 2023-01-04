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

package controllers

import base.SpecBaseWithAppPerSuite
import org.mockito.Mockito.when
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scheduler.SchedulerActions

import scala.concurrent.Future

class TriggerReferenceDataImportControllerSpec extends SpecBaseWithAppPerSuite {
  val mockSchedulerActions = mock[SchedulerActions]

  override def mocks = super.mocks :+ mockSchedulerActions

  override def guiceApplicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(bind[SchedulerActions].toInstance(mockSchedulerActions))

  "scheduleImport" - {
    "must return 200 when there is a request body and action is successfully scheduled" in {
      when(mockSchedulerActions.triggerReferenceDataImport()).thenReturn(Future.successful(true))

      val request =
        FakeRequest(POST, controllers.routes.TriggerReferenceDataImportController.scheduleImport.url)
          .withJsonBody(
            Json.obj()
          )

      val result = route(app, request).value

      status(result) mustEqual OK
    }
  }

  "must return 500 when scheduling of actions fails" in {
    when(mockSchedulerActions.triggerReferenceDataImport()).thenReturn(Future.successful(false))

    val request =
      FakeRequest(POST, controllers.routes.TriggerReferenceDataImportController.scheduleImport.url)
        .withJsonBody(
          Json.obj()
        )

    val result = route(app, request).value

    status(result) mustEqual INTERNAL_SERVER_ERROR
  }
}
