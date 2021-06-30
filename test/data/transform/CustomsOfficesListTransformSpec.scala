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

  val roles =
    "transform" - {
      "when the json matches the expected schema" - {
        "returns the transformed data as a JsSuccess" - {

          "with value for telephone from input source when present" in {
            val expected =
              Json
                .parse("""
                |{
                | "id": "AD000003",
                | "name": "CENTRAL CUSTOMS OFFICE",
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900"
                |}
                |""".stripMargin)
                .as[JsObject] ++ expectedRolesJson(currentTimetable1Denormalised)

            val result = Transformation(CustomsOfficesList).runTransform(inputCustomsOfficeJson())

            result.get mustEqual expected

          }

          "with null value for telephone when missing from input source" in {
            val expected =
              Json
                .parse("""
              |{
              | "id": "AD000003",
              | "name": "CENTRAL CUSTOMS OFFICE",
              | "countryId": "AD",
              | "phoneNumber": null
              |}
              |""".stripMargin)
                .as[JsObject] ++ expectedRolesJson(currentTimetable1Denormalised)

            val data = (__ \ "phoneNumber").json.prune.reads(inputCustomsOfficeJson()).get

            val result = Transformation(CustomsOfficesList).runTransform(data)

            result.get mustEqual expected

          }

          "with name from customsOfficeDetails" - {
            "as EN value when it is present" in {
              val expected =
                Json
                  .parse("""
                |{
                | "id": "AD000003",
                | "name": "CENTRAL CUSTOMS OFFICE",
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900"
                |}
                |""".stripMargin)
                  .as[JsObject] ++ expectedRolesJson(currentTimetable1Denormalised)

              val result =
                Transformation(CustomsOfficesList).runTransform(
                  inputCustomsOfficeJson(Seq(customsOfficeDetailsES, customsOfficeDetailsEN, customsOfficeDetailsFR))
                )

              result.get mustEqual expected

            }

            "as next available non EN name when EN is missing" in {
              val expected =
                Json
                  .parse("""
                |{
                | "id": "AD000003",
                | "name": "BUREAU CENTRAL DES DOUANES",
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900"
                |}
                |""".stripMargin)
                  .as[JsObject] ++ expectedRolesJson(currentTimetable1Denormalised)

              val result = Transformation(CustomsOfficesList).runTransform(inputCustomsOfficeJson(Seq(customsOfficeDetailsFR, customsOfficeDetailsES)))

              result.get mustEqual expected
            }

            "as null when there are no values provided" in {
              val expected =
                Json
                  .parse("""
                |{
                | "id": "AD000003",
                | "name": null,
                | "countryId": "AD",
                | "phoneNumber": "+ (376) 879900"
                |}
                |""".stripMargin)
                  .as[JsObject] ++ expectedRolesJson(currentTimetable1Denormalised)

              val result = Transformation(CustomsOfficesList).runTransform(inputCustomsOfficeJson(Seq.empty))

              result.get mustEqual expected
            }
          }

          "with values for roles from customsOfficeTimetable" - {
            "when there is a single timetable with a single competence" in {
              val expected =
                Json
                  .parse("""
                |{
                |  "id": "AD000003",
                |  "name": "CENTRAL CUSTOMS OFFICE",
                |  "countryId": "AD",
                |  "phoneNumber": "+ (376) 879900"
                |}
                |""".stripMargin)
                  .as[JsObject] ++ expectedRolesJson(currentTimetable3Denormalised)

              val result = Transformation(CustomsOfficesList).runTransform(inputCustomsOfficeJson(timeTable = Seq(currentTimetable3)))

              result.get mustEqual expected
            }

            "when there are multiple timetables with multiple competences" in {
              val expected =
                Json
                  .parse("""
                |{
                |  "id": "AD000003",
                |  "name": "CENTRAL CUSTOMS OFFICE",
                |  "countryId": "AD",
                |  "phoneNumber": "+ (376) 879900"
                |}
                |""".stripMargin)
                  .as[JsObject] ++ expectedRolesJson(currentTimetable1Denormalised ++ currentTimetable2Denormalised)

              val result = Transformation(CustomsOfficesList).runTransform(inputCustomsOfficeJson(timeTable = Seq(currentTimetable1, currentTimetable2)))

              result.get mustEqual expected

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
                val dataWithError = (__ \ mandatoryField).json.prune.reads(inputCustomsOfficeJson()).get

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

  def inputCustomsOfficeJson(
    customsOfficeDetails: Seq[JsObject] = Seq(customsOfficeDetailsES, customsOfficeDetailsEN, customsOfficeDetailsFR),
    timeTable: Seq[JsObject] = Seq(currentTimetable1)
  ): JsObject = {

    val customsOfficeDetailsJson = JsArray(customsOfficeDetails)
    val timeTableJson            = JsArray(timeTable)

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
            |  "customsOfficeDetails": ${customsOfficeDetailsJson.toString()},
            |  "customsOfficeTimetable": ${timeTableJson.toString()}
            |}
            |""".stripMargin)
      .as[JsObject]
  }

  def expectedRolesJson(roles: Seq[JsObject]): JsObject =
    Json.obj(
      "roles" -> JsArray(roles)
    )

  val customsOfficeDetailsES: JsObject =
    Json
      .parse("""|{
                |  "languageCode": "ES",
                |  "customsOfficeUsualName": "DESPATCHO CENTRAL DE ADUANAS",
                |  "streetAndNumber": "AVINGUDA FITER I ROSSELL, 2",
                |  "city": "ESCALDES-ENGORDANY",
                |  "prefixSuffixFlag": 0,
                |  "spaceToAdd": 0
                |}""".stripMargin)
      .as[JsObject]

  val customsOfficeDetailsEN: JsObject =
    Json
      .parse("""|{
                |  "languageCode": "EN",
                |  "customsOfficeUsualName": "CENTRAL CUSTOMS OFFICE",
                |  "streetAndNumber": "AVINGUDA FITER I ROSSELL, 2",
                |  "city": "ESCALDES - ENGORDANY",
                |  "prefixSuffixFlag": 0,
                |  "prefixSuffixLevel": "E",
                |  "prefixSuffixName": "Tulli",
                |  "spaceToAdd": 0
                |}""".stripMargin)
      .as[JsObject]

  val customsOfficeDetailsFR: JsObject =
    Json
      .parse("""|{
                |  "languageCode": "FR",
                |  "customsOfficeUsualName": "BUREAU CENTRAL DES DOUANES",
                |  "streetAndNumber": "AVINGUDA FITER I ROSSELL, 2",
                |  "city": "ESCALDES - ENGORDANY",
                |  "prefixSuffixFlag": 0,
                |  "spaceToAdd": 0
                |}""".stripMargin)
      .as[JsObject]

  val currentTimetable1: JsObject =
    Json
      .parse("""|{
                |  "seasonCode": 1,
                |  "seasonName": "ALL YEAR",
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
                |          "role": "AUT",
                |          "trafficType": "R"
                |        },
                |        {
                |          "role": "CAU",
                |          "trafficType": "R"
                |        }
                |      ]
                |    }
                |  ]
                |}""".stripMargin)
      .as[JsObject]

  val currentTimetable1Denormalised: Seq[JsObject] = Seq(
    Json
      .parse("""|{
                |  "seasonCode": 1,
                |  "seasonName": "ALL YEAR",
                |  "seasonStartDate": "20180101",
                |  "seasonEndDate": "20991231",
                |  "dayInTheWeekBeginDay": 1,
                |  "openingHoursTimeFirstPeriodFrom": "0800",
                |  "openingHoursTimeFirstPeriodTo": "1700",
                |  "openingHoursTimeSecondPeriodFrom": "1200",
                |  "openingHoursTimeSecondPeriodTo": "1800",
                |  "dayInTheWeekEndDay": 5,
                |  "role": "AUT",
                |  "trafficType": "R"
                |}""".stripMargin)
      .as[JsObject],
    Json
      .parse("""|{
                |  "seasonCode": 1,
                |  "seasonName": "ALL YEAR",
                |  "seasonStartDate": "20180101",
                |  "seasonEndDate": "20991231",
                |  "dayInTheWeekBeginDay": 1,
                |  "openingHoursTimeFirstPeriodFrom": "0800",
                |  "openingHoursTimeFirstPeriodTo": "1700",
                |  "openingHoursTimeSecondPeriodFrom": "1200",
                |  "openingHoursTimeSecondPeriodTo": "1800",
                |  "dayInTheWeekEndDay": 5,
                |  "role": "CAU",
                |  "trafficType": "R"
                |}""".stripMargin)
      .as[JsObject]
  )

  val currentTimetable2 =
    Json
      .parse("""|{
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
                |""".stripMargin)
      .as[JsObject]

  val currentTimetable2Denormalised: Seq[JsObject] = Seq(
    Json
      .parse("""|{
                |  "seasonCode": 2,
                |  "seasonName": "SEASON_2",
                |  "seasonStartDate": "20180101",
                |  "seasonEndDate": "20991231",
                |  "dayInTheWeekBeginDay": 1,
                |  "openingHoursTimeFirstPeriodFrom": "0800",
                |  "openingHoursTimeFirstPeriodTo": "1700",
                |  "openingHoursTimeSecondPeriodFrom": "1200",
                |  "openingHoursTimeSecondPeriodTo": "1800",
                |  "dayInTheWeekEndDay": 5,
                |  "role": "GUA",
                |  "trafficType": "R"
                |}""".stripMargin)
      .as[JsObject],
    Json
      .parse("""|{
                |  "seasonCode": 2,
                |  "seasonName": "SEASON_2",
                |  "seasonStartDate": "20180101",
                |  "seasonEndDate": "20991231",
                |  "dayInTheWeekBeginDay": 1,
                |  "openingHoursTimeFirstPeriodFrom": "0800",
                |  "openingHoursTimeFirstPeriodTo": "1700",
                |  "openingHoursTimeSecondPeriodFrom": "1200",
                |  "openingHoursTimeSecondPeriodTo": "1800",
                |  "dayInTheWeekEndDay": 5,
                |  "role": "REC",
                |  "trafficType": "R"
                |}""".stripMargin)
      .as[JsObject]
  )

  val currentTimetable3 =
    Json
      .parse("""|{
                |  "seasonCode": 1,
                |  "seasonName": "ALL YEAR",
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
                |          "role": "AUT",
                |          "trafficType": "R"
                |        }
                |      ]
                |    }
                |  ]
                |}""".stripMargin)
      .as[JsObject]

  val currentTimetable3Denormalised: Seq[JsObject] = Seq(
    Json
      .parse("""|{
                |  "seasonCode": 1,
                |  "seasonName": "ALL YEAR",
                |  "seasonStartDate": "20180101",
                |  "seasonEndDate": "20991231",
                |  "dayInTheWeekBeginDay": 1,
                |  "openingHoursTimeFirstPeriodFrom": "0800",
                |  "openingHoursTimeFirstPeriodTo": "1700",
                |  "openingHoursTimeSecondPeriodFrom": "1200",
                |  "openingHoursTimeSecondPeriodTo": "1800",
                |  "dayInTheWeekEndDay": 5,
                |  "role": "AUT",
                |  "trafficType": "R"
                |}""".stripMargin)
      .as[JsObject]
  )
}
