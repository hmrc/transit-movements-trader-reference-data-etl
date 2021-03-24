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

package data.filter

import java.time.LocalDate

import akka.NotUsed
import akka.stream.ActorAttributes
import akka.stream.Attributes
import akka.stream.Supervision
import akka.stream.scaladsl.Flow
import logging.TagUtil.SupervisionStrategyException
import models.ReferenceDataList
import models.ReferenceDataList.Constants.Common
import play.api.Logger
import play.api.libs.json.JsObject

case class FilterFlow(list: ReferenceDataList) {

  val logger: Logger = Logger(getClass.getSimpleName)

  private def isActiveRecord(state: String, activeFrom: Option[LocalDate]): Boolean =
    state == Common.valid && activeFrom
      .exists(_.isBefore(LocalDate.now().plusDays(1)))

  private val supervisionStrategy: Attributes = ActorAttributes.supervisionStrategy {
    case filteredException @ FilterFlowItemNotActiveException(_, state) =>
      logger.info(
        s"[supervisionStrategy] Filtering out item for '${list.listName}' where activeFrom: ${filteredException.activeFromAsString} and state: $state LIST_ITEM_FILTERED"
      )
      Supervision.resume
    case _ =>
      logger.warn(
        s"${SupervisionStrategyException.toString} An unexpected exception happened when trying to filter the Json item for '${list.listName}' UNEXPECTED_LIST_ITEM_FILTERING_EXCEPTION"
      )
      Supervision.resume
  }

  def flow: Flow[JsObject, JsObject, NotUsed] =
    Flow[JsObject]
      .map {
        jsObject =>
          val state      = (jsObject \ Common.state).as[String]
          val activeFrom = (jsObject \ Common.activeFrom).asOpt[LocalDate]
          if (isActiveRecord(state, activeFrom))
            jsObject
          else
            throw FilterFlowItemNotActiveException(activeFrom, state)
      }
      .addAttributes(supervisionStrategy)
}
