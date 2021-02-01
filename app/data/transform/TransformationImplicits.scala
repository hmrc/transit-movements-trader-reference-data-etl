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

package data.transform

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
import models.ReferenceDataList.Constants._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

trait TransformationImplicits {

  implicit val transformationCountryCodesFullList: Transformation[CountryCodesFullList.type] =
    Transformation
      .fromReads(
        (
          (__ \ CountryCodesFullListFieldNames.code).json.copyFrom((__ \ "countryCode").json.pick) and
            (__ \ Common.state).json.pickBranch and
            (__ \ Common.activeFrom).json.pickBranch and
            (__ \ Common.description).json.copyFrom(englishDescription)
        ).reduce
          .andThen(
            (__ \ Common.activeFrom).json.prune
          )
      )

  implicit val transformationCountryCodesCommonTransitList: Transformation[CountryCodesCommonTransitList.type] =
    Transformation
      .fromReads(
        (
          (__ \ CountryCodesCommonTransitListFieldNames.code).json.copyFrom((__ \ "countryCode").json.pick) and
            (__ \ Common.state).json.pickBranch and
            (__ \ Common.activeFrom).json.pickBranch and
            (__ \ Common.description).json.copyFrom(englishDescription)
        ).reduce
          .andThen(
            (__ \ Common.activeFrom).json.prune
          )
      )

  implicit val transformationCustomsOfficeList: Transformation[CustomsOfficesList.type] = {

    val customsOfficeDetailsEN: Reads[JsObject] = (__ \ "customsOfficeDetails").json.update(
      of[JsArray].flatMap[JsObject] {
        case JsArray(array) =>
          val englishDetails: Option[JsValue] = array.find(
            x => (x \ "languageCode").as[String].toLowerCase == "en"
          )
          englishDetails match {
            case Some(value: JsObject) => Reads.pure(value)
            case _                     => Reads.failed("Could not find element in array matching `en` language in path #/customsOfficeDetails")
          }

      }
    )

    Transformation
      .fromReads(
        (
          (__ \ CustomsOfficesListFieldNames.id).json.copyFrom((__ \ "referenceNumber").json.pick) and
            (__ \ Common.state).json.pickBranch and
            (__ \ Common.activeFrom).json.pickBranch and
            (__ \ "name").json.copyFrom(
              customsOfficeDetailsEN.andThen((__ \ "customsOfficeDetails" \ "customsOfficeUsualName").json.pick) orElse Reads.pure(JsNull)
            ) and
            (__ \ CustomsOfficesListFieldNames.countryId).json.copyFrom((__ \ "countryCode").json.pick) and
            (__ \ CustomsOfficesListFieldNames.phoneNumber).json.copyFrom((__ \ "phoneNumber").json.pick orElse Reads.pure(JsNull)) and
            (__ \ CustomsOfficesListFieldNames.roles).json.put(JsArray.empty)
        ).reduce
          .andThen((__ \ Common.state).json.prune)
          .andThen((__ \ Common.activeFrom).json.prune)
      )
  }

  implicit val transformationDocumentTypeCommonList: Transformation[DocumentTypeCommonList.type] =
    Transformation
      .fromReads(
        (
          (__ \ Common.state).json.pickBranch and
            (__ \ Common.activeFrom).json.pickBranch and
            (__ \ DocumentTypeCommonListFieldNames.code).json.copyFrom((__ \ "documentType").json.pick) and
            (__ \ Common.description).json.copyFrom(englishDescription) and
            (__ \ DocumentTypeCommonListFieldNames.transportDocument).json.copyFrom(
              (__ \ DocumentTypeCommonListFieldNames.transportDocument).json.pick.andThen(
                booleanFromIntString(DocumentTypeCommonList, (__ \ DocumentTypeCommonListFieldNames.transportDocument))
              )
            )
        ).reduce
          .andThen((__ \ Common.state).json.prune)
          .andThen((__ \ Common.activeFrom).json.prune)
      )

  private val simpleCodeDescriptionReads: Reads[JsObject] =
    (
      (__ \ Common.state).json.pickBranch and
        (__ \ Common.activeFrom).json.pickBranch and
        (__ \ PreviousDocumentTypeCommonListFieldNames.code).json.pickBranch and
        (__ \ Common.description).json.copyFrom(englishDescription)
    ).reduce
      .andThen((__ \ Common.state).json.prune)
      .andThen((__ \ Common.activeFrom).json.prune)

  implicit val transformationPreviousDocumentTypeCommonList: Transformation[PreviousDocumentTypeCommonList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

  implicit val transformationKindOfPackagesList: Transformation[KindOfPackagesList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

  implicit val transformationTransportModeList: Transformation[TransportModeList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

  implicit val transformationAdditionalInformationIdCommonList: Transformation[AdditionalInformationIdCommonList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

  implicit val transformationSpecificCircumstanceIndicatorList: Transformation[SpecificCircumstanceIndicatorList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

  implicit val transformationUnDangerousGoodsCodeList: Transformation[UnDangerousGoodsCodeList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

  implicit val transformationTransportChargesMethodOfPaymentList: Transformation[TransportChargesMethodOfPaymentList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

  implicit val transformationControlResultList: Transformation[ControlResultList.type] =
    Transformation.fromReads(simpleCodeDescriptionReads)

}
