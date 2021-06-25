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

import akka.NotUsed
import akka.stream.Attributes
import akka.stream.alpakka.json.scaladsl.JsonReader
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import data.config.StreamLoggingConfig
import javax.inject.Inject
import logging.StreamLoggerAdapter
import play.api.libs.json.JsObject
import play.api.libs.json.Json

private[data] class ReferenceDataJsonProjection @Inject() (streamLoggingConfig: StreamLoggingConfig) extends StreamLoggerAdapter {

  val (onElement, onFinish, onFailure) = streamLoggingConfig.loggingConfig(None)

  private val pathToNestedData = "$.data[*]"

  val dataElements: Flow[ByteString, JsObject, NotUsed] =
    JsonReader
      .select(pathToNestedData)
      .map(
        byteString => Json.parse(byteString.toArray).as[JsObject]
      )
      .log(loggerName)
      .withAttributes(
        Attributes
          .logLevels(
            onElement = onElement,
            onFinish = onFinish,
            onFailure = onFailure
          )
      )

}
