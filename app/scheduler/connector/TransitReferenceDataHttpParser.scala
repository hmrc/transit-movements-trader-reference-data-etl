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

package scheduler.connector

import logging.Logging
import play.api.http.Status.OK
import scheduler.connector.ErrorResponse.UnexpectedResponseStatus
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.HttpResponse

object TransitReferenceDataHttpParser extends Logging {

  type TransitReferenceDataResponse = Either[ErrorResponse, Boolean]

  implicit object TransitReferenceDataReads extends HttpReads[TransitReferenceDataResponse] {

    override def read(method: String, url: String, response: HttpResponse): TransitReferenceDataResponse =
      response.status match {
        case OK =>
          Right(true)
        case status =>
          Left(UnexpectedResponseStatus(status, s"Unexpected response: $status"))
      }
  }
}
