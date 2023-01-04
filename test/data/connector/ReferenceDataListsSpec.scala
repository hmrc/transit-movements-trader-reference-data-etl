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

package data.connector

import base.SpecBase
import models.SpecificCircumstanceIndicatorList
import models.CountryCodesFullList
import play.api.libs.json.Json

class ReferenceDataListsSpec extends SpecBase {

  "should read all known lists when there are only known lists" in {

    val testObject =
      Json.obj(
        "_links" -> Json.obj(
          "_self"                                    -> Json.obj("href" -> "self/path"),
          SpecificCircumstanceIndicatorList.listName -> Json.obj("href" -> "CircumstanceIndicatorList/path"),
          CountryCodesFullList.listName              -> Json.obj("href" -> "CountryCodesFullList/path")
        )
      )

    val expected = ReferenceDataLists(
      Map(
        SpecificCircumstanceIndicatorList -> "CircumstanceIndicatorList/path",
        CountryCodesFullList              -> "CountryCodesFullList/path"
      )
    )

    val result = testObject.as[ReferenceDataLists]

    result mustEqual expected
  }

  "should read all known lists when there are unknown lists" in {

    val testObject =
      Json.obj(
        "_links" -> Json.obj(
          "_self"                                    -> Json.obj("href" -> "self/path"),
          SpecificCircumstanceIndicatorList.listName -> Json.obj("href" -> "CircumstanceIndicatorList/path"),
          "UnknownListName"                          -> Json.obj("href" -> "UnknownListName/path"),
          CountryCodesFullList.listName              -> Json.obj("href" -> "CountryCodesFullList/path")
        )
      )

    val expected = ReferenceDataLists(
      Map(
        SpecificCircumstanceIndicatorList -> "CircumstanceIndicatorList/path",
        CountryCodesFullList              -> "CountryCodesFullList/path"
      )
    )

    val result = testObject.as[ReferenceDataLists]

    result mustEqual expected

  }

}
