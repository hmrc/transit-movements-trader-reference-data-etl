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

package models

import base.SpecBase
import play.api.libs.json._

class TriggerDataImportReqestSpec extends SpecBase {
  "deserialization from json" - {
    "when the job has the correct name and the import is for all lists" in {
      val json =
        Json.obj(
          "job"  -> "DataImport",
          "only" -> Seq.empty[JsObject]
        )

      json.as[TriggerDataImportReqest] mustEqual TriggerDataImportReqest.TriggerAllDataImports
    }

    "when the job has the correct name and the import is for specific lists, then the specified lists are ignored and we import all lists" in {
      val json =
        Json.obj(
          "job"  -> "DataImport",
          "only" -> Seq("CountryCodesFullList")
        )

      json.as[TriggerDataImportReqest] mustEqual TriggerDataImportReqest.TriggerAllDataImports
    }

    "when the job name is incorrect" in {
      val json =
        Json.obj(
          "job"  -> "Invalid",
          "only" -> Seq.empty[JsObject]
        )

      val result = json
        .validate[TriggerDataImportReqest]
        .asEither
        .left
        .value
        .map(_._1)

      result must contain theSameElementsAs Seq((__ \ "job"))
    }
  }
}
