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

package data.filter

import java.time.LocalDate

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.ActorAttributes
import org.apache.pekko.stream.Attributes
import org.apache.pekko.stream.Supervision
import org.apache.pekko.stream.scaladsl.Flow
import logging.LoggingIdentifiers.LIST_ITEM_FILTERED
import logging.LoggingIdentifiers.UNEXPECTED_LIST_ITEM_FILTERING_EXCEPTION
import models.ReferenceDataList
import models.ReferenceDataList.Constants.Common
import play.api.Logger
import play.api.libs.json.JsObject
import data.transform.Transformation

object FilterFlow {

  val logger: Logger = Logger(getClass.getSimpleName)

  // TODO: Remove cross-cutting logic and filter using Transformation[ReferenceDataList]#filter
  private def isActiveRecord(state: String, activeFrom: Option[LocalDate]): Boolean =
    state == Common.valid && activeFrom
      .exists(_.isBefore(LocalDate.now().plusDays(1)))

  private def supervisionStrategy(list: ReferenceDataList): Attributes = ActorAttributes.supervisionStrategy {
    case filteredException @ FilterFlowItemNotActiveException(_, state) =>
      logger.info(
        s"${LIST_ITEM_FILTERED.toString} Filtering out item for '${list.listName}' where activeFrom: ${filteredException.activeFromAsString} and state: $state"
      )
      Supervision.resume
    case _ =>
      logger.warn(
        s"${UNEXPECTED_LIST_ITEM_FILTERING_EXCEPTION.toString} An unexpected exception happened when trying to filter the Json item for '${list.listName}'"
      )
      Supervision.resume
  }

  def flow[A <: ReferenceDataList](list: A)(implicit transformation: Transformation[A]): Flow[JsObject, JsObject, NotUsed] =
    Flow[JsObject]
      .map {
        jsObject =>
          val state      = (jsObject \ Common.state).as[String]
          val activeFrom = (jsObject \ Common.activeFrom).asOpt[LocalDate]
          if (isActiveRecord(state, activeFrom) && transformation.isValid(jsObject))
            jsObject
          else
            throw FilterFlowItemNotActiveException(activeFrom, state)
      }
      .addAttributes(supervisionStrategy(list))
}
