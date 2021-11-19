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

package scheduler

import org.mockito.Matchers.any
import org.mockito.Mockito._
import base.SpecBase
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchedulerActionsSyncSpec extends SpecBase {

  val mockImportDataService = mock[services.ImportDataService]

  "when the import succeeds, then the service returns true" in {
    when(mockImportDataService.importReferenceData()(any())).thenReturn(Future.successful(true))

    val service = new SchedulerActionsSync(mockImportDataService)

    service.triggerReferenceDataImport().futureValue mustEqual true
  }

  "when the import fails, then the service returns false" in {
    when(mockImportDataService.importReferenceData()(any())).thenReturn(Future.successful(false))

    val service = new SchedulerActionsSync(mockImportDataService)

    service.triggerReferenceDataImport().futureValue mustEqual false
  }
}
