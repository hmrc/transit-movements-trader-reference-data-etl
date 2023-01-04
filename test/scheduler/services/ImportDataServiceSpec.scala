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

package scheduler.services

import base.SpecBaseWithAppPerSuite
import data.DataRetrieval
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import scheduler.connector.ErrorResponse.UnexpectedResponseStatus
import scheduler.connector.TransitReferenceDataConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportDataServiceSpec extends SpecBaseWithAppPerSuite {

  private val mockDataRetrieval = mock[DataRetrieval]
  private val mockConnector     = mock[TransitReferenceDataConnector]

  override def guiceApplicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRetrieval].toInstance(mockDataRetrieval),
        bind[TransitReferenceDataConnector].toInstance(mockConnector)
      )

  override def mocks: Seq[_] = super.mocks ++ Seq(mockDataRetrieval, mockConnector)

  ".importReferenceData" - {

    val referenceData = Seq(Json.obj("id" -> 1))

    "must import all reference data when there are no failures on retrieving the list or posting" in {

      when(mockDataRetrieval.getList(any())(any())) thenReturn Future.successful(referenceData)
      when(mockConnector.post(any(), any())) thenReturn Future.successful(Right(true))

      val service = app.injector.instanceOf[ImportDataService]

      val result = service.importReferenceData().futureValue

      result mustEqual true

      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesFullList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommonTransitList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommunityList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CustomsOfficesList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(DocumentTypeCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(PreviousDocumentTypeCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(KindOfPackagesList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(TransportModeList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(AdditionalInformationIdCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(SpecificCircumstanceIndicatorList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(UnDangerousGoodsCodeList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(TransportChargesMethodOfPaymentList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(ControlResultList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommonTransitOutsideCommunityList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCustomsOfficeLists))(any())
      verify(mockDataRetrieval, times(15)).getList(any())(any())

      verify(mockConnector, times(1)).post(eqTo(CountryCodesFullList), eqTo(referenceData))
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCommonTransitList), eqTo(referenceData))
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCommunityList), eqTo(referenceData))
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
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCommonTransitOutsideCommunityList), eqTo(referenceData))
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCustomsOfficeLists), eqTo(referenceData))
      verify(mockConnector, times(15)).post(any(), any())

    }

    "must try to import all lists, even if retrieving data for some lists fails" in {

      when(mockDataRetrieval.getList(any())(any())).thenReturn(
        Future.successful(referenceData),
        Future.failed(new Exception("foo")),
        Future.successful(referenceData),
        Future.failed(new Exception("foo"))
      )

      val service = app.injector.instanceOf[ImportDataService]

      val result = service.importReferenceData().futureValue

      result mustEqual false

      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesFullList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommonTransitList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommunityList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CustomsOfficesList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(DocumentTypeCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(PreviousDocumentTypeCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(KindOfPackagesList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(TransportModeList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(AdditionalInformationIdCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(SpecificCircumstanceIndicatorList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(UnDangerousGoodsCodeList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(TransportChargesMethodOfPaymentList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(ControlResultList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommonTransitOutsideCommunityList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCustomsOfficeLists))(any())
      verify(mockDataRetrieval, times(15)).getList(any())(any())

      verify(mockConnector, times(2)).post(any(), any())
    }

    "must try to import all lists, even if posting some data to transit-reference-data fails" in {

      val referenceData = Seq(Json.obj("id" -> 1))
      when(mockDataRetrieval.getList(any())(any())).thenReturn(Future.successful(referenceData))
      when(mockConnector.post(any(), any())).thenReturn(
        Future.successful(Left(UnexpectedResponseStatus(500, "foo"))),
        Future.successful(Right(true)),
        Future.successful(Left(UnexpectedResponseStatus(500, "foo")))
      )

      val service = app.injector.instanceOf[ImportDataService]

      val result = service.importReferenceData().futureValue

      result mustEqual false

      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesFullList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommonTransitList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommunityList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CustomsOfficesList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(DocumentTypeCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(PreviousDocumentTypeCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(KindOfPackagesList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(TransportModeList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(AdditionalInformationIdCommonList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(SpecificCircumstanceIndicatorList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(UnDangerousGoodsCodeList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(TransportChargesMethodOfPaymentList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(ControlResultList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCommonTransitOutsideCommunityList))(any())
      verify(mockDataRetrieval, times(1)).getList(eqTo(CountryCodesCustomsOfficeLists))(any())
      verify(mockDataRetrieval, times(15)).getList(any())(any())

      verify(mockConnector, times(1)).post(eqTo(CountryCodesFullList), eqTo(referenceData))
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCommonTransitList), eqTo(referenceData))
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCommunityList), eqTo(referenceData))
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
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCommonTransitOutsideCommunityList), eqTo(referenceData))
      verify(mockConnector, times(1)).post(eqTo(CountryCodesCustomsOfficeLists), eqTo(referenceData))
      verify(mockConnector, times(15)).post(any(), any())

    }

  }
}
