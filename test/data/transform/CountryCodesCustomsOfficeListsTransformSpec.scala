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

package data.transform

import base.SpecBase
import models.CountryCodesCustomsOfficeLists
import play.api.libs.json.Json
import play.api.libs.json._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CountryCodesCustomsOfficeListsTransformSpecs extends SpecBase {
  import CountryCodesCustomsOfficeListsTransformSpecs._

  "transform" - {

    "when the json matches the expected schema" - {

      "transforms data as a JsSuccess JsObject and keeps the English description" in {

        val expected =
          """
          |{
          |  "code": "AD",
          |  "description": "Andorra",
          |  "countryRegimeCode": "TOC"
          |}
          |""".stripMargin

        val result = Transformation(CountryCodesCustomsOfficeLists).runTransform(validData1)

        result.get mustEqual Json.parse(expected)

      }

    }

    "when the json doesn't match the expected schema" - {

      "returns a JsError parse error if the json doesn't match the expected schema" - {

        "when missing countryCode" in {

          val data =
            """ 
            |{
            |  "state": "valid",
            |  "activeFrom": "2020-01-23",
            |  "tccEntryDate": "19000101",
            |  "nctsEntryDate": "19000101",
            |  "geoNomenclatureCode": "043",
            |  "countryRegimeCode": "TOC",
            |  "description": {
            |    "en": "Andorra",
            |    "tt": "Andorra"
            |  }
            |}
            |""".stripMargin

          val result =
            Transformation(CountryCodesCustomsOfficeLists).transform
              .reads(Json.parse(data))
              .asEither
              .left
              .value

          result.length mustEqual 1
          val (errorPath, _) = result.head
          errorPath mustEqual (__ \ "countryCode")

        }

        "when missing #/description/en field" in {
          val data =
            """ 
            |{
            |  "state": "valid",
            |  "activeFrom": "2020-01-23",
            |  "countryCode": "AD",
            |  "tccEntryDate": "19000101",
            |  "nctsEntryDate": "19000101",
            |  "geoNomenclatureCode": "043",
            |  "countryRegimeCode": "TOC",
            |  "description": {
            |    "tt": "Andorra"
            |  }
            |}
            |""".stripMargin

          val result =
            Transformation(CountryCodesCustomsOfficeLists).transform
              .reads(Json.parse(data))
              .asEither
              .left
              .value

          result.length mustEqual 1
          val (errorPath, _) = result.head
          errorPath mustEqual (__ \ "description" \ "en")
        }

        "when missing countryRegimeCode" in {

          val result =
            Transformation(CountryCodesCustomsOfficeLists).transform
              .reads(dataWithErrorMissingRegime)
              .asEither
              .left
              .value

          result.length mustEqual 1
          val (errorPath, _) = result.head
          errorPath mustEqual (__ \ "countryRegimeCode")

        }

      }
    }
  }

}

object CountryCodesCustomsOfficeListsTransformSpecs {

  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val today: String                 = LocalDate.now().format(dateFormat)
  val tomorrow: String              = LocalDate.now().plusDays(1).format(dateFormat)

  val validData1: JsObject =
    Json
      .parse(""" 
      |{
      |  "state": "valid",
      |  "activeFrom": "2020-01-23",
      |  "countryCode": "AD",
      |  "tccEntryDate": "19000101",
      |  "nctsEntryDate": "19000101",
      |  "geoNomenclatureCode": "043",
      |  "countryRegimeCode": "TOC",
      |  "description": {
      |    "en": "Andorra",
      |    "tt": "Andorra"
      |  }
      |}
      |""".stripMargin)
      .as[JsObject]

  val expected1: JsObject =
    Json
      .parse("""
      |{
      |  "code": "AD",
      |  "state": "valid",
      |  "description": "Andorra"
      |}
      |""".stripMargin)
      .as[JsObject]

  val validData2: JsObject =
    Json
      .parse(""" 
             |{
             |  "state": "valid",
             |  "activeFrom": "2015-07-01",
             |  "countryCode": "TT",
             |  "tccEntryDate": "19000101",
             |  "nctsEntryDate": "19000101",
             |  "geoNomenclatureCode": "472",
             |  "countryRegimeCode": "OTH",
             |  "description": {
             |      "en": "Trinidad and Tobago"
             |  }
             |}
             |""".stripMargin)
      .as[JsObject]

  val expected2: JsObject =
    Json
      .parse("""
        |{
        |  "code": "TT",
        |  "state": "valid",
        |  "description": "Trinidad and Tobago"
        |}
        |""".stripMargin)
      .as[JsObject]

  val invalidData: JsObject =
    Json
      .parse("""
               |{
               |  "state": "invalid",
               |  "activeFrom": "2015-07-01",
               |  "countryCode": "YY",
               |  "tccEntryDate": "19000101",
               |  "nctsEntryDate": "19000101",
               |  "geoNomenclatureCode": "472",
               |  "countryRegimeCode": "OTH",
               |  "description": {
               |      "en": "Shrewsbury"
               |  }
               |}
               |""".stripMargin)
      .as[JsObject]

  val activeFromInFutureData: JsObject =
    Json
      .parse("""
               |{
               |  "state": "valid",
               |  "activeFrom": "2222-07-01",
               |  "countryCode": "TF",
               |  "tccEntryDate": "19000101",
               |  "nctsEntryDate": "19000101",
               |  "geoNomenclatureCode": "472",
               |  "countryRegimeCode": "OTH",
               |  "description": {
               |      "en": "Somewhere"
               |  }
               |}
               |""".stripMargin)
      .as[JsObject]

  val invalidAndActiveFromInFutureData: JsObject =
    Json
      .parse("""
               |{
               |  "state": "invalid",
               |  "activeFrom": "2222-07-01",
               |  "countryCode": "SF",
               |  "tccEntryDate": "19000101",
               |  "nctsEntryDate": "19000101",
               |  "geoNomenclatureCode": "472",
               |  "countryRegimeCode": "OTH",
               |  "description": {
               |      "en": "A Country"
               |  }
               |}
               |""".stripMargin)
      .as[JsObject]

  val validAndActiveFromTodayData: JsObject =
    Json
      .parse(s"""
                |{
                |  "state": "valid",
                |  "activeFrom": "$today",
                |  "countryCode": "TD",
                |  "tccEntryDate": "19000101",
                |  "nctsEntryDate": "19000101",
                |  "geoNomenclatureCode": "472",
                |  "countryRegimeCode": "OTH",
                |  "description": {
                |      "en": "Today"
                |  }
                |}
                |""".stripMargin)
      .as[JsObject]

  val validAndActiveFromTomorrowData: JsObject =
    Json
      .parse(s"""
                |{
                |  "state": "valid",
                |  "activeFrom": "$tomorrow",
                |  "countryCode": "TM",
                |  "tccEntryDate": "19000101",
                |  "nctsEntryDate": "19000101",
                |  "geoNomenclatureCode": "472",
                |  "countryRegimeCode": "OTH",
                |  "description": {
                |      "en": "Tomorrow"
                |  }
                |}
                |""".stripMargin)
      .as[JsObject]

  val expected3: JsObject =
    Json
      .parse("""
               |{
               |  "code": "TD",
               |  "state": "valid",
               |  "description": "Today"
               |}
               |""".stripMargin)
      .as[JsObject]

  val dataWithErrorMissingRegime: JsObject =
    Json
      .parse(""" 
      |{
      |  "activeFrom": "2020-01-23",
      |  "countryCode": "AD",
      |  "tccEntryDate": "19000101",
      |  "nctsEntryDate": "19000101",
      |  "geoNomenclatureCode": "043",
      |  "description": {
      |    "en": "Andorra",
      |    "tt": "Andorra"
      |  }
      |}
      |""".stripMargin)
      .as[JsObject]

}
