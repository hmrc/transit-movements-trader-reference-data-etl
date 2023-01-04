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

package models

import base.SpecBase
import play.api.libs.json.Json

class IntermediateCustomsOfficeSpec extends SpecBase {

  val roles1 = Seq(
    Json.obj("role" -> "role1.1", "trafficType" -> "trafficType1"),
    Json.obj("role" -> "role1.2", "trafficType" -> "trafficType2")
  )

  val roles2 = Seq(
    Json.obj("role" -> "role2.1", "trafficType" -> "trafficType1"),
    Json.obj("role" -> "role2.2", "trafficType" -> "trafficType2")
  )

  val line1 = TimeTableLines(Json.obj("timeTableLine" -> "timeTableLine1"), roles1)
  val line2 = TimeTableLines(Json.obj("timeTableLine" -> "timeTableLine2"), roles2)
  val lines = Seq(line1, line2)

  val timeTable1 = CustomsOfficeTimeTable(
    Json.obj("customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue1"),
    lines
  )

  val timeTable2 = CustomsOfficeTimeTable(
    Json.obj("customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue2"),
    lines
  )
  val timeTables = Seq(timeTable1, timeTable2)

  val intermediateCustomsOffice = IntermediateCustomsOffice(
    Json.obj("customsOfficeOtherKey" -> "customsOfficeOtherValue"),
    timeTables
  )

  "denormalises timetable rows into individual objects with role details and timetable details" in {

    val expectedJson = Json.obj(
      "customsOfficeOtherKey" -> "customsOfficeOtherValue",
      "roles" -> Seq(
        Json.obj(
          "role"                           -> "role1.1",
          "trafficType"                    -> "trafficType1",
          "timeTableLine"                  -> "timeTableLine1",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue1"
        ),
        Json.obj(
          "role"                           -> "role1.2",
          "trafficType"                    -> "trafficType2",
          "timeTableLine"                  -> "timeTableLine1",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue1"
        ),
        Json.obj(
          "role"                           -> "role2.1",
          "trafficType"                    -> "trafficType1",
          "timeTableLine"                  -> "timeTableLine2",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue1"
        ),
        Json.obj(
          "role"                           -> "role2.2",
          "trafficType"                    -> "trafficType2",
          "timeTableLine"                  -> "timeTableLine2",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue1"
        ),
        Json.obj(
          "role"                           -> "role1.1",
          "trafficType"                    -> "trafficType1",
          "timeTableLine"                  -> "timeTableLine1",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue2"
        ),
        Json.obj(
          "role"                           -> "role1.2",
          "trafficType"                    -> "trafficType2",
          "timeTableLine"                  -> "timeTableLine1",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue2"
        ),
        Json.obj(
          "role"                           -> "role2.1",
          "trafficType"                    -> "trafficType1",
          "timeTableLine"                  -> "timeTableLine2",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue2"
        ),
        Json.obj(
          "role"                           -> "role2.2",
          "trafficType"                    -> "trafficType2",
          "timeTableLine"                  -> "timeTableLine2",
          "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue2"
        )
      )
    )

    Json.toJsObject(intermediateCustomsOffice) mustEqual expectedJson

  }

  "deserialises to a IntermediateCustomsOffice" in {
    val data =
      Json.obj(
        "customsOfficeOtherKey" -> "customsOfficeOtherValue",
        "customsOfficeTimetable" -> Seq(
          Json.obj(
            "customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue",
            "customsOfficeTimetableLine" -> Seq(
              Json.obj(
                "timeTableLine" -> "timeTableLine1",
                "customsOfficeRoleTrafficCompetence" -> Seq(
                  Json.obj("role" -> "role1.1", "trafficType" -> "trafficType1"),
                  Json.obj("role" -> "role1.2", "trafficType" -> "trafficType2")
                )
              )
            )
          )
        )
      )

    val expected = IntermediateCustomsOffice(
      Json.obj("customsOfficeOtherKey" -> "customsOfficeOtherValue"),
      Seq(
        CustomsOfficeTimeTable(
          Json.obj("customsOfficeTimetableOtherKey" -> "customsOfficeTimetableOtherValue"),
          Seq(line1)
        )
      )
    )
    data.as[IntermediateCustomsOffice] mustEqual expected
  }

}
