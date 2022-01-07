/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import base.SpecBase
import play.api.libs.json._

class TriggerDataImportReqestSpec extends SpecBase {
  "deserialization from json" - {
    "to TriggerAllDataImports when there are no listName specifed" in {
      val json = Json.obj()

      json.as[TriggerDataImportReqest] mustEqual TriggerDataImportReqest.TriggerAllDataImports
    }

    "to TriggerAllDataImports when the import is for specific lists" in {
      val json =
        Json.obj(
          "listNames" -> Seq("CountryCodesFullList")
        )

      json.as[TriggerDataImportReqest] mustEqual TriggerDataImportReqest.TriggerAllDataImports
    }
  }
}
