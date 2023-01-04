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

import models.AdditionalInformationIdCommonList
import models.ControlResultList
import models.CountryCodesCommonTransitList
import models.CountryCodesCommonTransitOutsideCommunityList
import models.CountryCodesFullList
import models.CountryCodesCommunityList
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
import models.IntermediateCustomsOffice
import play.api.Logger
import models.CountryCodesCustomsOfficeLists

import models.ReferenceDataList

trait TransformationImplicits {

  private def commonCountryTransformation[T <: ReferenceDataList](code: String): Transformation[T] = Transformation
    .instance(
      (
        (__ \ code).json.copyFrom((__ \ "countryCode").json.pick) and
          (__ \ Common.state).json.pickBranch and
          (__ \ Common.activeFrom).json.pickBranch and
          (__ \ Common.description).json.copyFrom(englishDescription)
      ).reduce
        .andThen(
          (__ \ Common.activeFrom).json.prune
        ),
      Seq.empty
    )

  implicit val transformationCountryCodesFullList: Transformation[CountryCodesFullList.type] =
    commonCountryTransformation[CountryCodesFullList.type](CountryCodesFullListFieldNames.code)

  implicit val transformationCountryCodesCustomsOfficeList: Transformation[CountryCodesCustomsOfficeLists.type] =
    Transformation
      .instance(
        (
          (__ \ CountryCodesFullListFieldNames.code).json.copyFrom((__ \ "countryCode").json.pick) and
            (__ \ Common.description).json.copyFrom(englishDescription) and
            (__ \ CountryCodesCustomsOfficeListsFieldNames.countryRegimeCode).json.pickBranch
        ).reduce,
        Seq.empty
      )

  implicit val transformationCountryCodesCommonTransitList: Transformation[CountryCodesCommonTransitList.type] =
    commonCountryTransformation[CountryCodesCommonTransitList.type](CountryCodesCommonTransitListFieldNames.code)

  implicit val countryCodesCommonTransitOutsideCommunityList: Transformation[CountryCodesCommonTransitOutsideCommunityList.type] =
    commonCountryTransformation[CountryCodesCommonTransitOutsideCommunityList.type](CountryCodesCommonTransitOutsideCommunityListFieldNames.code)

  implicit val countryCodesCommunityList: Transformation[CountryCodesCommunityList.type] =
    commonCountryTransformation[CountryCodesCommunityList.type](CountryCodesCommunityListFieldNames.code)

  implicit val transformationCustomsOfficeList: Transformation[CustomsOfficesList.type] = {
    val customsOfficeDetailsEN: Reads[JsObject] = (__ \ "customsOfficeDetails").json.update(
      of[JsArray].flatMap[JsObject] {
        case JsArray(array) =>
          val englishDetails: Option[JsValue] = array.find(
            x => (x \ "languageCode").as[String].toLowerCase == "en"
          )

          englishDetails match {
            case Some(value: JsObject) => Reads.pure(value)
            case _ =>
              array.headOption
                .map(
                  value => Reads.pure(value.as[JsObject])
                )
                .getOrElse(Reads.failed("Transformation failed due to empty array of customsOfficesDetails"))
          }
      }
    )

    val denormaliseTimetable: Reads[JsObject] =
      Reads(
        jsValue =>
          if ((jsValue \ "customsOfficeTimetable").isEmpty) {
            Logger(getClass.getCanonicalName).warn("[denormaliseTimetable] found customs office without customsOfficeTimetable")
            jsValue.validate[JsObject].map(_ ++ Json.obj("roles" -> Json.arr()))
          } else {
            jsValue.validate(implicitly[Reads[IntermediateCustomsOffice]]).map(Json.toJsObject(_))
          }
      )

    val selectFields: Reads[JsObject] = (
      (__ \ CustomsOfficesListFieldNames.id).json.copyFrom((__ \ "referenceNumber").json.pick) and
        (__ \ Common.state).json.pickBranch and
        (__ \ Common.activeFrom).json.pickBranch and
        (__ \ "name").json.copyFrom(
          customsOfficeDetailsEN.andThen((__ \ "customsOfficeDetails" \ "customsOfficeUsualName").json.pick) orElse Reads.pure(JsNull)
        ) and
        (__ \ CustomsOfficesListFieldNames.countryId).json.copyFrom((__ \ "countryCode").json.pick) and
        (__ \ CustomsOfficesListFieldNames.phoneNumber).json.copyFrom((__ \ "phoneNumber").json.pick orElse Reads.pure(JsNull)) and
        (__ \ CustomsOfficesListFieldNames.roles).json.pickBranch
    ).reduce
      .andThen((__ \ Common.state).json.prune)
      .andThen((__ \ Common.activeFrom).json.prune)

    Transformation.instance(denormaliseTimetable.andThen(selectFields), Seq.empty)
  }

  implicit val transformationDocumentTypeCommonList: Transformation[DocumentTypeCommonList.type] =
    Transformation
      .instance(
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
          .andThen((__ \ Common.activeFrom).json.prune),
        Seq.empty
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
    Transformation.instance(simpleCodeDescriptionReads, Seq.empty)

  implicit val transformationKindOfPackagesList: Transformation[KindOfPackagesList.type] =
    Transformation.instance(simpleCodeDescriptionReads, Seq.empty)

  implicit val transformationTransportModeList: Transformation[TransportModeList.type] =
    Transformation.instance(
      simpleCodeDescriptionReads,
      Seq(
        jsObj => (jsObj \ "code").validate[String].map(_.length <= 2).getOrElse(false)
      )
    )

  implicit val transformationAdditionalInformationIdCommonList: Transformation[AdditionalInformationIdCommonList.type] =
    Transformation.instance(simpleCodeDescriptionReads, Seq.empty)

  implicit val transformationSpecificCircumstanceIndicatorList: Transformation[SpecificCircumstanceIndicatorList.type] =
    Transformation.instance(simpleCodeDescriptionReads, Seq.empty)

  implicit val transformationUnDangerousGoodsCodeList: Transformation[UnDangerousGoodsCodeList.type] =
    Transformation.instance(simpleCodeDescriptionReads, Seq.empty)

  implicit val transformationTransportChargesMethodOfPaymentList: Transformation[TransportChargesMethodOfPaymentList.type] =
    Transformation.instance(simpleCodeDescriptionReads, Seq.empty)

  implicit val transformationControlResultList: Transformation[ControlResultList.type] =
    Transformation.instance(simpleCodeDescriptionReads, Seq.empty)

}
