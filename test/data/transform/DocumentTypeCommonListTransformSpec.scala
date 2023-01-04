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

import java.time.format.DateTimeFormatter

import base.SpecBase
import models.DocumentTypeCommonList
import models.ReferenceDataList.Constants.Common._
import models.ReferenceDataList.Constants.DocumentTypeCommonListFieldNames._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.libs.json._

class DocumentTypeCommonListTransformSpec extends SpecBase with ScalaCheckPropertyChecks {

  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  val validData =
    Json
      .parse("""
      |{
      |  "state": "valid",
      |  "activeFrom": "2019-12-01",
      |  "documentType": "18",
      |  "transportDocument": "0",
      |  "description": {
      |    "en": "Movement certificate A.TR.1",
      |    "es": "Certificado de circulación ATR 1"
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
          |  "code": "18",
          |  "description": "Movement certificate A.TR.1",
          |  "transportDocument": false
          |}
          |""".stripMargin

        val result = Transformation(DocumentTypeCommonList).runTransform(validData)

        result.get mustEqual Json.parse(expected)

      }
    }

    "when the json doesn't match the expected schema" - {
      val mandatoryFields: Gen[JsPath] = Gen.oneOf(
        Seq(
          (__ \ state),
          (__ \ activeFrom),
          (__ \ description \ en),
          (__ \ transportDocument)
        )
      )

      "returns a JsError parse error" - {
        "when top level mandatory fields are missing" in {
          forAll(mandatoryFields) {
            mandatoryField =>
              val dataWithError = mandatoryField.json.prune.reads(validData).get

              val result =
                Transformation(DocumentTypeCommonList).transform
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

  "filter" ignore {

    "returns a JsSuccess of None if the country is invalid" ignore {}

    "returns a JsSuccess of None if the activeFrom date is in the future" ignore {}

  }

}
