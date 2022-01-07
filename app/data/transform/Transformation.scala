/*
 * Copyright 2022 HM Revenue & Customs
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

import models.ReferenceDataList
import play.api.libs.json.JsObject
import play.api.libs.json.JsResult
import play.api.libs.json.Reads

trait Transformation[A] {

  /** The transformation that will be applied to the object
    *
    * @return The reads to transform the payload
    */
  def transform: Reads[JsObject]

  /** Run the transformation for an input
    *
    * @param JsObject to be transformed
    *
    * @return Result of transformation
    */
  def runTransform(input: JsObject): JsResult[JsObject] =
    transform.reads(input)

  /** The function that can test a pre-transformed object,
    * which allows business rules can be used to filter out items.
    */
  def isValid(jsObj: JsObject): Boolean

}

object Transformation extends TransformationImplicits {

  def apply[A: Transformation]: Transformation[A] = implicitly[Transformation[A]]

  def apply[A <: ReferenceDataList](a: A)(implicit ev: Transformation[A]): Transformation[A] = ev

  def instance[A <: ReferenceDataList](reads: Reads[JsObject], filters: Seq[JsObject => Boolean]): Transformation[A] =
    new Transformation[A] {
      override def transform: Reads[JsObject] = reads

      override def isValid(jsObj: JsObject): Boolean =
        filters
          .forall(
            predicate => predicate(jsObj)
          )
    }

}
