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

package data.transform

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import logging.Logging
import models.ReferenceDataList
import play.api.libs.json.JsError
import play.api.libs.json.JsObject
import play.api.libs.json.JsSuccess
import logging.LoggingIdentifiers.DataTransformationFailure

class TransformationFlow[A](list: ReferenceDataList, transformation: Transformation[A]) extends Logging {

  val flow: Flow[JsObject, JsObject, NotUsed] =
    Flow[JsObject]
      .map(
        transformation.transform.reads
      )
      .map {
        case JsSuccess(value, _) => Option(value)
        case JsError(errors) =>
          logger.warn(s"${DataTransformationFailure.toString} Error with transformation of data item for list `${list.listName}`, with error: $errors")
          None
      }
      .flatMapConcat {
        case Some(value) => Source.single(value)
        case None        => Source.empty
      }

}

object TransformationFlow {

  def apply[A](list: ReferenceDataList, transformation: Transformation[A]): TransformationFlow[A] =
    new TransformationFlow(list, transformation)

}
