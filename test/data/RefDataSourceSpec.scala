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

package data

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import base.SpecBase
import data.ReferenceDataJsonProjectionSpec.formatAsReferenceDataByteString
import data.connector.RefDataConnector
import logging.TestStreamLoggingConfig
import models.ReferenceDataList
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.OWrites

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RefDataSourceSpec extends SpecBase {

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  "reads the returned json and returns the nested sequence of values in `data`" in {
    case class TestObject(int: Int)
    implicit val owrites: OWrites[TestObject] = o => Json.obj("int" -> o.int)

    val testData     = formatAsReferenceDataByteString(Seq(TestObject(1), TestObject(2)))
    val expectedData = List(Json.obj("int" -> 1), Json.obj("int" -> 2))

    val listName = ReferenceDataList.values.head

    val mockDataConnector = mock[RefDataConnector]
    when(mockDataConnector.get(eqTo(listName))).thenReturn(Future.successful(Some(testData)))
    when(mockDataConnector.getAsSource(eqTo(listName))).thenReturn(Future.successful(Some(Source.single(testData))))

    val referenceDataJsonProjection = new ReferenceDataJsonProjection(TestStreamLoggingConfig)
    val sut                         = new RefDataSource(mockDataConnector, referenceDataJsonProjection)
    val source                      = sut(listName).futureValue.value

    source
      .runWith(TestSink.probe[JsObject])
      .request(2)
      .expectNextN(all = expectedData)
      .expectComplete()
  }

}
