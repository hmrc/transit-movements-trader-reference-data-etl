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

package data

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Keep
import org.apache.pekko.stream.testkit.scaladsl.TestSink
import org.apache.pekko.stream.testkit.scaladsl.TestSource
import org.apache.pekko.util.ByteString
import base.SpecBase
import logging.TestStreamLoggingConfig
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Writes

class ReferenceDataJsonProjectionSpec extends SpecBase {
  import ReferenceDataJsonProjectionSpec._

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  val referenceDataJsonProjection = new ReferenceDataJsonProjection(TestStreamLoggingConfig)

  "projects and returns all values in the nested sequence of values in the `data` array" in {

    val expectedData = List(Json.obj("int" -> 1), Json.obj("int" -> 2))
    val testData     = formatAsReferenceDataByteString(expectedData)

    val (pub, sub) =
      TestSource
        .probe[ByteString]
        .via(referenceDataJsonProjection.dataElements)
        .toMat(TestSink.probe[JsObject])(Keep.both)
        .run()

    sub.request(expectedData.length + 1)
    pub.sendNext(testData)
    sub.expectNextN(expectedData)
  }

  "stream returns an error when values in the `data` array are not objects" in {

    val expectedData = List("one")
    val testData     = formatAsReferenceDataByteString(expectedData)

    val (pub, sub) =
      TestSource
        .probe[ByteString]
        .via(referenceDataJsonProjection.dataElements)
        .toMat(TestSink.probe[JsObject])(Keep.both)
        .run()

    sub.request(expectedData.length + 1)
    pub.sendNext(testData)
    val error = sub.expectError()

    error.getMessage().contains("JsResultException") mustEqual true
  }

}

object ReferenceDataJsonProjectionSpec {

  def formatAsReferenceDataByteString[A: Writes](data: Seq[A]): ByteString =
    ByteString(formatAsReferenceDataJson(data).toString())

  def formatAsReferenceDataJson[A: Writes](data: Seq[A]): JsObject =
    Json
      .obj(
        "_links" -> Json.obj("self" -> "foo"),
        "meta"   -> Json.obj("meta1" -> "meta1Value"),
        "id"     -> "idValue",
        "data"   -> Json.toJson(data)
      )
}
