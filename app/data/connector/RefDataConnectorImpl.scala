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

package data.connector

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.data._
import cats.implicits._
import javax.inject.Inject
import models.ReferenceDataList
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.libs.ws.WSResponse
import play.mvc.Http.HeaderNames

private[connector] class RefDataConnectorImpl @Inject() (ws: WSClient, connectorConfig: ConnectorConfig)(implicit ec: ExecutionContext)
    extends RefDataConnector {

  private def wsGet(url: String): Future[WSResponse] =
    ws.url(url).withHttpHeaders(HeaderNames.USER_AGENT -> connectorConfig.appName).get()

  private def wsStream(url: String): Future[WSResponse] =
    ws.url(url).withHttpHeaders(HeaderNames.USER_AGENT -> connectorConfig.appName).stream()

  override def get(listName: ReferenceDataList): Future[Option[ByteString]] = {
    val url = connectorConfig.customsReferenceData.urlWithBaseUrl("/lists")

    (for {
      response <- OptionT.liftF(wsGet(url))
      lists = response.json.as[ReferenceDataLists]
      listRelativePath <- OptionT.fromOption[Future](lists.getPath(listName))
      listUrl = connectorConfig.customsReferenceData.fromRelativePath(listRelativePath)
      listData <- OptionT.liftF(wsGet(listUrl))
    } yield listData.bodyAsBytes).value
  }

  override def getAsSource(listName: ReferenceDataList): Future[Option[Source[ByteString, _]]] = {
    val url = connectorConfig.customsReferenceData.urlWithBaseUrl("/lists")

    (for {
      response <- OptionT.liftF(wsGet(url))
      lists = response.json.as[ReferenceDataLists]
      listRelativePath <- OptionT.fromOption[Future](lists.getPath(listName))
      listUrl = connectorConfig.customsReferenceData.fromRelativePath(listRelativePath)
      listData <- OptionT.liftF(wsStream(listUrl))
    } yield listData.bodyAsSource).value
  }

}
