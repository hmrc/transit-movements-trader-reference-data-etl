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

package scheduler.services

import data.DataRetrieval
import data.transform.Transformation
import javax.inject.Inject
import logging.Logging
import models._
import scheduler.connector.TransitReferenceDataConnector

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ImportDataService @Inject() (
  dataRetrieval: DataRetrieval,
  transitReferenceDataConnector: TransitReferenceDataConnector
) extends Logging {

  // Remove logging here, logging happens in importLists method
  def importReferenceData()(implicit ec: ExecutionContext): Future[Boolean] =
    importLists().map {
      results =>
        if (results.contains(false)) {
          logger.warn("Import complete with some failures")
          false
        } else {
          logger.info("Import completed successfully")
          true
        }
    }

  private def importLists()(implicit ec: ExecutionContext): Future[List[Boolean]] =
    Future.sequence(
      List(
        importList(CountryCodesFullList),
        importList(CountryCodesCommonTransitList),
        importList(CustomsOfficesList),
        importList(DocumentTypeCommonList),
        importList(PreviousDocumentTypeCommonList),
        importList(KindOfPackagesList),
        importList(TransportModeList),
        importList(AdditionalInformationIdCommonList),
        importList(SpecificCircumstanceIndicatorList),
        importList(UnDangerousGoodsCodeList),
        importList(TransportChargesMethodOfPaymentList),
        importList(ControlResultList)
      )
    )


  //  case object ImportException
  //  case object ImportFailure

  private def importList[A <: ReferenceDataList](list: A)(implicit ec: ExecutionContext, ev: Transformation[A]): Future[Boolean] = {
    for {
      data   <- dataRetrieval.getList(list)
      result <- transitReferenceDataConnector.post(list, data)
    } yield result match {
      case Right(_) =>
        logger.info(s"Import of ${list.listName} complete")
        true
      case Left(value) =>
        logger.warn(s"Import of ${list.listName} failed with message ${value.body}")
        false
    }
  } recover {
    case e: Exception =>
      logger.error(s"An error occurred trying to import ${list.listName}", e)
      false
  }
}
