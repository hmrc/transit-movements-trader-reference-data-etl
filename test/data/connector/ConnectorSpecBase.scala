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

import base.SpecBaseWithAppPerSuite
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterAll
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.guice.GuiceableModule

trait ConnectorSpecBase extends SpecBaseWithAppPerSuite with BeforeAndAfterAll {

  /** The path to the config for the external HTTP call
    *
    * @return The name of the config key for the external service
    */
  protected def portConfigKey: String

  /** An overrideable hook that allows for overriding the configuration
    * of  guice module in the test suite
    *
    * @return Seq of modules binding that will be used by [[org.scalatestplus.play.guice.GuiceOneAppPerSuite]]
    */
  protected def bindings: Seq[GuiceableModule] = Seq.empty

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  protected val appName: String = "transit-movements-trader-reference-data-etl"

  override def guiceApplicationBuilder: GuiceApplicationBuilder =
    super.guiceApplicationBuilder
      .configure(
        portConfigKey -> server.port()
      )
      .overrides(bindings: _*)

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEachBlocks: Seq[() => Unit] =
    super.beforeEachBlocks ++ Seq(
      () => server.resetAll()
    )

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

}
