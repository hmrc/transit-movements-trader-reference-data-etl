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
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.stream.testkit.scaladsl.TestSink
import org.apache.pekko.util.ByteString
import base.SpecBaseWithAppPerSuite
import data.connector.RefDataConnector
import models.CountryCodesFullList
import models.TransportModeList
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.libs.json.Json

import scala.concurrent.Future

class DataRetrievalSpec extends SpecBaseWithAppPerSuite {
  import data.transform.CountryCodesFullListTransformSpec._

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  val mockRefDataConnector: RefDataConnector = mock[RefDataConnector]

  override val mocks: Seq[_] = super.mocks :+ mockRefDataConnector

  override def guiceApplicationBuilder: GuiceApplicationBuilder =
    super.guiceApplicationBuilder
      .overrides(bind[RefDataConnector].toInstance(mockRefDataConnector))

  "streamList" - {

    "given a ReferenceDataList, returns a stream of the elements for the list" in {
      val sut: DataRetrieval = app.injector.instanceOf[DataRetrieval]

      val listName = CountryCodesFullList

      val testElements = List(validData1, validData2)

      val testData: ByteString = ReferenceDataJsonProjectionSpec.formatAsReferenceDataByteString(testElements)

      when(mockRefDataConnector.getAsSource(eqTo(listName)))
        .thenReturn(Future.successful(Option(Source.single(testData))))

      val source = sut.streamList(listName).futureValue.value

      source
        .runWith(TestSink.probe[JsObject])
        .request(2)
        .expectNextN(List(expected1, expected2))
        .expectComplete()

    }

    "given a ReferenceDataList, returns a stream of the elements, filtering out the invalid states for the list" in {
      val sut: DataRetrieval = app.injector.instanceOf[DataRetrieval]

      val listName = CountryCodesFullList

      val testElements = List(
        validData1,
        validData2,
        invalidData,
        activeFromInFutureData,
        invalidAndActiveFromInFutureData,
        validAndActiveFromTodayData,
        validAndActiveFromTomorrowData
      )

      val testData: ByteString = ReferenceDataJsonProjectionSpec.formatAsReferenceDataByteString(testElements)

      when(mockRefDataConnector.getAsSource(eqTo(listName)))
        .thenReturn(Future.successful(Option(Source.single(testData))))

      val source = sut.streamList(listName).futureValue.value

      source
        .runWith(TestSink.probe[JsObject])
        .request(7)
        .expectNextN(List(expected1, expected2, expected3))
        .expectComplete()

    }

  }

  "getList" - {

    "given a ReferenceDataList, returns a List of the elements for the list name" in {
      val sut: DataRetrieval = app.injector.instanceOf[DataRetrieval]

      val listName = CountryCodesFullList

      val testElements = List(validData1, validData2)

      val testData: ByteString = ReferenceDataJsonProjectionSpec.formatAsReferenceDataByteString(testElements)

      when(mockRefDataConnector.getAsSource(eqTo(listName)))
        .thenReturn(Future.successful(Option(Source.single(testData))))

      val result = sut.getList(listName).futureValue

      result mustEqual List(expected1, expected2)

    }

    "given a ReferenceDataList, returns a List of only the elements that are valid" in {
      val sut: DataRetrieval = app.injector.instanceOf[DataRetrieval]

      val listName = TransportModeList

      val testElements = List(
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
          .as[JsObject],
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
      )

      val testData: ByteString = ReferenceDataJsonProjectionSpec.formatAsReferenceDataByteString(testElements)

      when(mockRefDataConnector.getAsSource(eqTo(listName)))
        .thenReturn(Future.successful(Option(Source.single(testData))))

      val result = sut.getList(listName).futureValue

      result mustEqual List(
        Json
          .parse("""
            |{
            |  "code": "10",
            |  "description": "Sea transport"
            |}
            |""".stripMargin)
          .as[JsObject]
      )

    }

    "given a ReferenceDataList, returns an empty List of the elements for the list name" in {
      val sut: DataRetrieval = app.injector.instanceOf[DataRetrieval]

      val listName = CountryCodesFullList

      when(mockRefDataConnector.getAsSource(eqTo(listName)))
        .thenReturn(Future.successful(None))

      val result = sut.getList(listName).futureValue

      result must be(empty)

    }

  }

}
