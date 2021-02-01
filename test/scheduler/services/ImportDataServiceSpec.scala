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
import models.AdditionalInformationIdCommonList
import models.ControlResultList
import models.CountryCodesCommonTransitList
import models.CountryCodesFullList
import models.CustomsOfficesList
import models.DocumentTypeCommonList
import models.KindOfPackagesList
import models.PreviousDocumentTypeCommonList
import models.SpecificCircumstanceIndicatorList
import models.TransportChargesMethodOfPaymentList
import models.TransportModeList
import models.UnDangerousGoodsCodeList
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import scheduler.connector.ErrorResponse.UnexpectedResponseStatus
import scheduler.connector.TransitReferenceDataConnector

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ImportDataServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  private val mockDataRetrieval = mock[DataRetrieval]
  private val mockConnector     = mock[TransitReferenceDataConnector]

  private val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRetrieval].toInstance(mockDataRetrieval),
        bind[TransitReferenceDataConnector].toInstance(mockConnector)
      )

  override def beforeEach(): Unit = {
    reset(mockDataRetrieval)
    reset(mockConnector)
    super.beforeEach()
  }

  ".importReferenceData" - {

    "must import all reference data" in {

      val referenceData = Seq(Json.obj("id" -> 1))
      when(mockDataRetrieval.getList(any())(any())) thenReturn Future.successful(referenceData)
      when(mockConnector.post(any(), any())) thenReturn Future.successful(Right(true))

      val app = appBuilder.build()

      running(app) {

        val service = app.injector.instanceOf[ImportDataService]

        val result = service.importReferenceData().futureValue

        result mustEqual true
        verify(mockConnector, times(1)).post(eqTo(CountryCodesFullList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(CountryCodesCommonTransitList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(CustomsOfficesList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(DocumentTypeCommonList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(PreviousDocumentTypeCommonList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(KindOfPackagesList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(TransportModeList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(AdditionalInformationIdCommonList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(SpecificCircumstanceIndicatorList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(UnDangerousGoodsCodeList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(TransportChargesMethodOfPaymentList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(ControlResultList), eqTo(referenceData))
        verify(mockConnector, times(12)).post(any(), any())
      }
    }

    "must try to import all lists, even if retrieving data for some lists fails" in {

      when(mockDataRetrieval.getList(any())(any())) thenReturn Future.failed(new Exception("foo"))

      val app = appBuilder.build()

      running(app) {

        val service = app.injector.instanceOf[ImportDataService]

        val result = service.importReferenceData().futureValue

        result mustEqual false
        verify(mockDataRetrieval, times(12)).getList(any())(any())
        verify(mockConnector, times(0)).post(any(), any())
      }
    }

    "must try to import all lists, even if posting some data to transit-reference-data fails" in {

      val referenceData = Seq(Json.obj("id" -> 1))
      when(mockDataRetrieval.getList(any())(any())) thenReturn Future.successful(referenceData)
      when(mockConnector.post(any(), any())) thenReturn Future.successful(Left(UnexpectedResponseStatus(500, "foo")))

      val app = appBuilder.build()

      running(app) {

        val service = app.injector.instanceOf[ImportDataService]

        val result = service.importReferenceData().futureValue

        result mustEqual false
        verify(mockConnector, times(1)).post(eqTo(CountryCodesFullList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(CountryCodesCommonTransitList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(CustomsOfficesList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(DocumentTypeCommonList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(PreviousDocumentTypeCommonList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(KindOfPackagesList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(TransportModeList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(AdditionalInformationIdCommonList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(SpecificCircumstanceIndicatorList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(UnDangerousGoodsCodeList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(TransportChargesMethodOfPaymentList), eqTo(referenceData))
        verify(mockConnector, times(1)).post(eqTo(ControlResultList), eqTo(referenceData))
        verify(mockConnector, times(12)).post(any(), any())
      }
    }

  }
}
