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

import play.api.libs.json._
import play.api.libs.functional.syntax._

final case class TimeTableLines(
  otherFields: JsObject,
  competences: Seq[JsObject]
)

final case class CustomsOfficeTimeTable(
  otherFields: JsObject,
  lines: Seq[TimeTableLines]
)

final case class IntermediateCustomsOffice(
  otherFields: JsObject,
  timetables: Seq[CustomsOfficeTimeTable]
)

object IntermediateCustomsOffice {

  implicit val writes: OWrites[IntermediateCustomsOffice] = OWrites {
    case IntermediateCustomsOffice(otherFields, timeTables) =>
      val roles: Seq[JsObject] = timeTables.flatMap {
        tt =>
          tt.lines.flatMap {
            line =>
              line.competences.map {
                competence =>
                  competence ++ line.otherFields ++ tt.otherFields
              }

          }
      }

      otherFields ++ Json.obj("roles" -> roles)
  }

  implicit val readsTimeTableLines: Reads[TimeTableLines] =
    (
      (__ \ "customsOfficeRoleTrafficCompetence").json.prune and
        (__ \ "customsOfficeRoleTrafficCompetence").read[Seq[JsObject]]
    )(TimeTableLines.apply _)

  implicit val readsCustomsOfficeTimeTable: Reads[CustomsOfficeTimeTable] =
    (
      (__ \ "customsOfficeTimetableLine").read[Seq[TimeTableLines]] and
        (__ \ "customsOfficeTimetableLine").json.prune
    )(
      (x, y) => CustomsOfficeTimeTable(y, x)
    )

  implicit val readsIntermediateCustomsOffice: Reads[IntermediateCustomsOffice] =
    (
      (__ \ "customsOfficeTimetable").json.prune and
        (__ \ "customsOfficeTimetable").read[Seq[CustomsOfficeTimeTable]]
    )(IntermediateCustomsOffice.apply _)

}
