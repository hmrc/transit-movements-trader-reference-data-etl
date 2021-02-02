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

package logging

import akka.event.Logging.LogLevel
import akka.event.{Logging => AkkaLogging}
import com.google.inject.AbstractModule
import data.config.StreamLoggingConfig

class TestLoggingModule extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[StreamLoggingConfig]).toInstance(TestStreamLoggingConfig)
}

object TestStreamLoggingConfig extends StreamLoggingConfig {

  /**
    * Configuration for the logging level of AkkaStream components. This allows for global
    * application configuration, or stream component specific configuration.
    *
    * @param streamComponentName if provided, this will be used for this component over the
    *                            global value at `data.stream.logging.onElement.level`,
    *                            `data.stream.logging.onFinish.level` or
    *                            `data.stream.logging.onFailure.level`
    *
    * @return (onElement, onFinish, onFailure)
    */
  override def loggingConfig(streamComponentName: Option[String]): (LogLevel, LogLevel, LogLevel) =
    (AkkaLogging.levelFor("off").get, AkkaLogging.levelFor("off").get, AkkaLogging.levelFor("off").get)
}
