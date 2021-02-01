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

package data.connector

import models.ReferenceDataList
import play.api.libs.json.Reads._
import play.api.libs.json._

private[data] case class ReferenceDataLists(listPathMappings: Map[ReferenceDataList, String]) {

  def getPath(listName: ReferenceDataList): Option[String] =
    listPathMappings.get(listName)

}

private[data] object ReferenceDataLists {

  private val hrefTransform: Reads[JsString] =
    (__ \\ "href").json.pick[JsString]

  implicit val readReferenceDataLists: Reads[ReferenceDataLists] =
    (__ \ "_links")
      .read[Map[String, JsObject]]
      .map(_.flatMap {
        case (listName, path) =>
          ReferenceDataList.mappings
            .get(listName)
            .map(refDatList => (refDatList, path.transform(hrefTransform).get.value))
      })
      .map(ReferenceDataLists(_))

}
