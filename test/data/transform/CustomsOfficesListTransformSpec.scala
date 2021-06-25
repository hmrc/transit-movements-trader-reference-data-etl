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

package data.transform

import java.time.format.DateTimeFormatter

import base.SpecBase
import models.CustomsOfficesList
import org.scalacheck.Gen
import org.scalacheck.Shrink
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.libs.json._

class CustomsOfficesListTransformSpec extends SpecBase with ScalaCheckPropertyChecks {
  import CustomsOfficesListTransformSpec._

  implicit def dontShrink[A]: Shrink[A] = Shrink.shrinkAny

  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  "transform" - {
    "when the json matches the expected schema" - {
      "returns the transformed data as a JsSuccess" - {

        "when telephone is present" in {
          val expected =
            """
                |{
                | "id": "AD000003",
                | "name": "CENTRAL CUSTOMS OFFICE",
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900",
                | "roles": []
                |}
                |""".stripMargin

          val result = Transformation(CustomsOfficesList).runTransform(customsOfficeJson())

          result.get mustEqual Json.parse(expected)

        }

        "when telephone is missing" in {
          val expected =
            """
              |{
              | "id": "AD000003",
              | "name": "CENTRAL CUSTOMS OFFICE",
              | "countryId": "AD",
              | "phoneNumber": null,
              | "roles": []
              |}
              |""".stripMargin

          val data = (__ \ "phoneNumber").json.prune.reads(customsOfficeJson()).get

          val result = Transformation(CustomsOfficesList).runTransform(data)

          result.get mustEqual Json.parse(expected)

        }

        "when customsOfficeDetails EN" - {
          "is present" in {
            val expected =
              """
                |{
                | "id": "AD000003",
                | "name": "CENTRAL CUSTOMS OFFICE",
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900",
                | "roles": []
                |}
                |""".stripMargin

            val result =
              Transformation(CustomsOfficesList).runTransform(customsOfficeJson(Seq(customsOfficeDetailsES, customsOfficeDetailsEN, customsOfficeDetailsFR)))

            result.get mustEqual Json.parse(expected)

          }

          "is missing and picks next available non EN name" in {
            val expected =
              """
                |{
                | "id": "AD000003",
                | "name": "BUREAU CENTRAL DES DOUANES",
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900",
                | "roles": []
                |}
                |""".stripMargin

            val result = Transformation(CustomsOfficesList).runTransform(customsOfficeJson(Seq(customsOfficeDetailsFR, customsOfficeDetailsES)))

            result.get mustEqual Json.parse(expected)
          }

          "is missing" in {
            val expected =
              """
                |{
                | "id": "AD000003",
                | "name": null,
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900",
                | "roles": []
                |}
                |""".stripMargin

            val result = Transformation(CustomsOfficesList).runTransform(customsOfficeJson(Seq.empty))

            result.get mustEqual Json.parse(expected)
          }
        }

      }
    }

    "when the json doesn't match the expected schema" - {

      "returns a JsError parse error" - {
        val mandatoryFields = Gen.oneOf(
          Seq(
            "referenceNumber",
            "state",
            "activeFrom",
            "countryCode"
          )
        )

        "when top level mandatory fields are missing" in {
          forAll(mandatoryFields) {
            mandatoryField =>
              val dataWithError = (__ \ mandatoryField).json.prune.reads(customsOfficeJson()).get

              val result =
                Transformation(CustomsOfficesList).transform
                  .reads(dataWithError)
                  .asEither
                  .left
                  .value

              result.length mustEqual 1
              val (errorPath, _) = result.head
              errorPath mustEqual (__ \ mandatoryField)

          }

        }

      }
    }

  }

  "filter" ignore {

    "returns a JsSuccess of None if the country is invalid" ignore {}

    "returns a JsSuccess of None if the activeFrom date is in the future" ignore {}

  }

}

object CustomsOfficesListTransformSpec {

  def customsOfficeJson(
    customsOfficeDetails: Seq[String] = Seq(customsOfficeDetailsES, customsOfficeDetailsEN, customsOfficeDetailsFR),
    timeTable: Seq[String] = Seq(currentTimetable1)
  ): JsObject = {

    def containsValidJsObject(xs: Seq[String]): Boolean = {
      xs.map(
        x => Json.parse(x).as[JsObject]
      )
      true
    }

    require(containsValidJsObject(customsOfficeDetails), "Customs Office must be a JSON Object")
    require(containsValidJsObject(timeTable), "Timetable must be a JSON Object")

    val customsOfficeDetailsJson: String = customsOfficeDetails.mkString("[", ",", "]")
    val timeTableJson: String            = timeTable.mkString("[", ",", "]")

    Json
      .parse(s"""
            |{
            |  "state": "valid",
            |  "activeFrom": "2019-01-01",
            |  "referenceNumber": "AD000003",
            |  "referenceNumberMainOffice": "AD000003",
            |  "referenceNumberHigherAuthority": "AD000003",
            |  "referenceNumberCompetentAuthorityOfEnquiry": "AD000003",
            |  "referenceNumberCompetentAuthorityOfRecovery": "AD000003",
            |  "countryCode": "AD",
            |  "unLocodeId": "ALV",
            |  "nctsEntryDate": "20070614",
            |  "nearestOffice": "CH002621",
            |  "postalCode": "AD700",
            |  "phoneNumber": "+ (376) 879900",
            |  "faxNumber": "+ (376) 860360",
            |  "eMailAddress": "Helpdesk_ncts@andorra.ad",
            |  "geoInfoCode": "Q",
            |  "traderDedicated": 1,
            |  "regionCode": "BW",
            |  "telexNumber": "+358005671",
            |  "customsOfficeSpecificNotes": [
            |    {
            |      "specificNotesCode": "SN0002"
            |    },
            |    {
            |      "specificNotesCode": "SN0006"
            |    },
            |    {
            |      "specificNotesCode": "SN0022"
            |    }
            |  ],
            |  "dedicatedTrader": [
            |    {
            |      "languageCode": "EN",
            |      "name": "H M Revenue & Customs"
            |    }
            |  ],
            |  "customsOfficeDetails": $customsOfficeDetailsJson,
            |  "customsOfficeTimetable": $timeTableJson
            |}
            |""".stripMargin)
      .as[JsObject]
  }

  val customsOfficeDetailsES: String =
    """
     |{
     |  "languageCode": "ES",
     |  "customsOfficeUsualName": "DESPATCHO CENTRAL DE ADUANAS",
     |  "streetAndNumber": "AVINGUDA FITER I ROSSELL, 2",
     |  "city": "ESCALDES-ENGORDANY",
     |  "prefixSuffixFlag": 0,
     |  "spaceToAdd": 0
     |}""".stripMargin

  val customsOfficeDetailsEN: String =
    """
     |{
     |  "languageCode": "EN",
     |  "customsOfficeUsualName": "CENTRAL CUSTOMS OFFICE",
     |  "streetAndNumber": "AVINGUDA FITER I ROSSELL, 2",
     |  "city": "ESCALDES - ENGORDANY",
     |  "prefixSuffixFlag": 0,
     |  "prefixSuffixLevel": "E",
     |  "prefixSuffixName": "Tulli",
     |  "spaceToAdd": 0
     |}""".stripMargin

  val customsOfficeDetailsFR: String =
    """
     |{
     |  "languageCode": "FR",
     |  "customsOfficeUsualName": "BUREAU CENTRAL DES DOUANES",
     |  "streetAndNumber": "AVINGUDA FITER I ROSSELL, 2",
     |  "city": "ESCALDES - ENGORDANY",
     |  "prefixSuffixFlag": 0,
     |  "spaceToAdd": 0
     |}""".stripMargin

  val currentTimetable1: String =
    """
      |    {
      |      "seasonCode": 1,
      |      "seasonName": "ALL YEAR",
      |      "seasonStartDate": "20180101",
      |      "seasonEndDate": "20991231",
      |      "customsOfficeTimetableLine": [
      |        {
      |          "dayInTheWeekBeginDay": 1,
      |          "openingHoursTimeFirstPeriodFrom": "0800",
      |          "openingHoursTimeFirstPeriodTo": "1700",
      |          "openingHoursTimeSecondPeriodFrom": "1200",
      |          "openingHoursTimeSecondPeriodTo": "1800",
      |          "dayInTheWeekEndDay": 5,
      |          "customsOfficeRoleTrafficCompetence": [
      |            {
      |              "role": "AUT",
      |              "trafficType": "R"
      |            },
      |            {
      |              "role": "CAU",
      |              "trafficType": "R"
      |            }
      |          ]
      |        }
      |      ]
      |    }
      |""".stripMargin

  val currentTimetable2 =
    """
      |{
      |  "seasonCode": 2,
      |  "seasonName": "SEASON_2",
      |  "seasonStartDate": "20180101",
      |  "seasonEndDate": "20991231",
      |  "customsOfficeTimetableLine": [
      |    {
      |      "dayInTheWeekBeginDay": 1,
      |      "openingHoursTimeFirstPeriodFrom": "0800",
      |      "openingHoursTimeFirstPeriodTo": "1700",
      |      "openingHoursTimeSecondPeriodFrom": "1200",
      |      "openingHoursTimeSecondPeriodTo": "1800",
      |      "dayInTheWeekEndDay": 5,
      |      "customsOfficeRoleTrafficCompetence": [
      |        {
      |          "role": "GUA",
      |          "trafficType": "R"
      |        },
      |        {
      |          "role": "REC",
      |          "trafficType": "R"
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin

  val futureTimetable =
    """
      |{
      |  "seasonCode": 3,
      |  "seasonName": "SEASON_3",
      |  "seasonStartDate": "29990101",
      |  "seasonEndDate": "29991231",
      |  "customsOfficeTimetableLine": [
      |    {
      |      "dayInTheWeekBeginDay": 1,
      |      "openingHoursTimeFirstPeriodFrom": "0800",
      |      "openingHoursTimeFirstPeriodTo": "1700",
      |      "openingHoursTimeSecondPeriodFrom": "1200",
      |      "openingHoursTimeSecondPeriodTo": "1800",
      |      "dayInTheWeekEndDay": 5,
      |      "customsOfficeRoleTrafficCompetence": [
      |        {
      |          "role": "GUA",
      |          "trafficType": "R"
      |        },
      |        {
      |          "role": "REC",
      |          "trafficType": "R"
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin

  val pastTimetable =
    """
      |{
      |  "seasonCode": 4,
      |  "seasonName": "SEASON_4",
      |  "seasonStartDate": "19180101",
      |  "seasonEndDate": "19991231",
      |  "customsOfficeTimetableLine": [
      |    {
      |      "dayInTheWeekBeginDay": 1,
      |      "openingHoursTimeFirstPeriodFrom": "0800",
      |      "openingHoursTimeFirstPeriodTo": "1700",
      |      "openingHoursTimeSecondPeriodFrom": "1200",
      |      "openingHoursTimeSecondPeriodTo": "1800",
      |      "dayInTheWeekEndDay": 5,
      |      "customsOfficeRoleTrafficCompetence": [
      |        {
      |          "role": "GUA",
      |          "trafficType": "R"
      |        },
      |        {
      |          "role": "REC",
      |          "trafficType": "R"
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin

}
