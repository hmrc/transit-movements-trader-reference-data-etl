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

import TransitReferenceDataHttpParser.TransitReferenceDataReads
import TransitReferenceDataHttpParser.TransitReferenceDataResponse
import config.Service
import javax.inject.Inject
import models.ReferenceDataList
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class TransitReferenceDataConnector @Inject() (httpClient: HttpClient, config: ConnectorConfig)(implicit ec: ExecutionContext) {

  private val serviceUrl: Service = config.transitMovementsTraderReferenceData

  def post(list: ReferenceDataList, data: Seq[JsObject]): Future[TransitReferenceDataResponse] =
    httpClient.POST(serviceUrl.urlWithBaseUrl(s"data/${list.listName}"), data)(implicitly, TransitReferenceDataReads, HeaderCarrier(), implicitly)
}
