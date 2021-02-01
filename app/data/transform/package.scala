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

package data

import models.ReferenceDataList
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsError
import play.api.libs.json.JsPath
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.__

package object transform {

  val englishDescription: Reads[JsValue] =
    (__ \ "description" \ "en").json.pick

  def booleanFromIntString(listName: ReferenceDataList, path: JsPath): Reads[JsBoolean] =
    Reads[JsBoolean] {
      case JsString("1") => JsSuccess(JsBoolean(true))
      case JsString("0") => JsSuccess(JsBoolean(false))
      case JsString(_) =>
        JsError(s"Error in parsing ${listName.listName} at path ${path.toString()}, expected value of 0 or 1, got string")
      case x =>
        JsError(s"Error in parsing ${listName.listName} at path ${path.toString()}, expected value of 0 or 1, got ${x.getClass.getSimpleName}")
    }

}
