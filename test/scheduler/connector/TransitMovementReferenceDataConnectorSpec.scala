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

package scheduler.connector

import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import generators.ArbitraryInstances
import models.ReferenceDataList
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.OptionValues
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import scheduler.connector.ErrorResponse.UnexpectedResponseStatus
import utils.WireMockHelper

class TransitMovementReferenceDataConnectorSpec
    extends AnyFreeSpec
    with Matchers
    with WireMockHelper
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with ScalaCheckPropertyChecks
    with ArbitraryInstances {

  private def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.transit-movements-trader-reference-data.port" -> server.port
      )

  ".post" - {

    "must return Right(true) when the submission is accepted" in {

      val data = Seq(Json.obj())
      val list = arbitrary[ReferenceDataList].sample.value

      val app = appBuilder.build()
      running(app) {

        val connector = app.injector.instanceOf[TransitReferenceDataConnector]

        server.stubFor(
          post(urlEqualTo(s"/transit-movements-trader-reference-data/data/${list.listName}"))
            .willReturn(ok)
        )

        val result = connector.post(list, data).futureValue
        result.value mustEqual true
      }
    }

    "must return Left(UnexpectedResponseStatus) when the submission returns an error code" in {

      val errorCode = Gen.oneOf(404, 429, 500, 501, 502, 503).sample.value
      val data      = Seq(Json.obj())
      val list      = arbitrary[ReferenceDataList].sample.value

      val app = appBuilder.build()

      running(app) {
        val connector = app.injector.instanceOf[TransitReferenceDataConnector]

        server.stubFor(
          post(urlEqualTo(s"/transit-movements-trader-reference-data/data/${list.listName}"))
            .willReturn(status(errorCode))
        )

        val result = connector.post(list, data).futureValue
        result.left.value mustEqual UnexpectedResponseStatus(errorCode, s"Unexpected response: $errorCode")
      }
    }
  }
}
