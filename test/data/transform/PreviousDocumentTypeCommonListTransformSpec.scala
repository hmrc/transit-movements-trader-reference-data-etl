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

package data.transform

import java.time.format.DateTimeFormatter

import base.SpecBase
import models.PreviousDocumentTypeCommonList
import models.ReferenceDataList.Constants.Common._
import models.ReferenceDataList.Constants.DocumentTypeCommonListFieldNames._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.libs.json._

class PreviousDocumentTypeCommonListTransformSpec extends SpecBase with ScalaCheckPropertyChecks {

  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  val validData =
    Json
      .parse("""
      |{
      |  "state": "valid",
      |  "activeFrom": "2019-11-15",
      |  "code": "100",
      |  "description": {
      |    "en": "enVal",
      |    "fr": "frVal"
      |  }
      |}
      |""".stripMargin)
      .as[JsObject]

  "transform" - {
    "when the json matches the expected schema" - {
      "returns the transformed data as a JsSuccess when there is a description" in {
        val expectedData =
          Json
            .parse("""
                     |{
                     |  "code": "100",
                     |  "description": "enVal"
                     |}
                     |""".stripMargin)
            .as[JsObject]

        val result = Transformation(PreviousDocumentTypeCommonList).runTransform(validData)

        result.get mustEqual expectedData

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
                Transformation(PreviousDocumentTypeCommonList).transform
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
