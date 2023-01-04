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
import models.TransportModeList
import models.ReferenceDataList.Constants.Common._
import models.ReferenceDataList.Constants.TransportModeListFieldNames._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.libs.json._

class TransportModeListTransformSpec extends SpecBase with ScalaCheckPropertyChecks {

  val validData =
    Json
      .parse("""
      |{
      |  "state": "valid",
      |  "activeFrom": "2020-05-30",
      |  "code": "1",
      |  "description": {
      |    "el": "Θαλάσσια μεταφορά",
      |    "en": "Sea transport"
      |  }
      |}
      |""".stripMargin)
      .as[JsObject]

  "transform" - {
    "when the json matches the expected schema" - {
      "returns the transformed data as a JsSuccess" in {

        val expected =
          """
          |{
          |  "code": "1",
          |  "description": "Sea transport"
          |}
          |""".stripMargin

        val result = Transformation(TransportModeList).runTransform(validData)

        result.get mustEqual Json.parse(expected)

      }
    }

    "when the json doesn't match the expected schema" - {
      val mandatoryFields: Gen[JsPath] = Gen.oneOf(
        Seq(
          (__ \ state),
          (__ \ activeFrom),
          (__ \ code),
          (__ \ description \ en)
        )
      )

      "returns a JsError parse error" - {
        "when top level mandatory fields are missing" in {
          forAll(mandatoryFields) {
            mandatoryField =>
              val dataWithError = mandatoryField.json.prune.reads(validData).get

              val result =
                Transformation(TransportModeList).transform
                  .reads(dataWithError)
                  .asEither
                  .left
                  .value

              result.length mustEqual 1
              val (errorPath, _) = result.head
              errorPath mustEqual mandatoryField

          }

        }

      }
    }
  }

  "filter" - {

    "returns false if the transport mode is longer than 2 characters" in {
      val invalidTransportCode =
        Json
          .parse("""
            |{
            |  "state": "valid",
            |  "activeFrom": "2020-05-30",
            |  "code": "10R",
            |  "description": {
            |    "el": "Θαλάσσια μεταφορά",
            |    "en": "Sea transport"
            |  }
            |}
            |""".stripMargin)
          .as[JsObject]

      Transformation(TransportModeList).isValid(invalidTransportCode) mustEqual false
    }

    "returns true if the transport mode is 2 characters or less" in {
      val invalidTransportCode =
        Json
          .parse("""
            |{
            |  "state": "valid",
            |  "activeFrom": "2020-05-30",
            |  "code": "10",
            |  "description": {
            |    "el": "Θαλάσσια μεταφορά",
            |    "en": "Sea transport"
            |  }
            |}
            |""".stripMargin)
          .as[JsObject]

      Transformation(TransportModeList).isValid(invalidTransportCode) mustEqual true
    }

  }

}
