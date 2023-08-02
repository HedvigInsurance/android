package com.hedvig.android.feature.insurances.data

import com.hedvig.android.core.ui.insurance.ContractType
import com.hedvig.android.core.ui.insurance.ContractType.*
import giraffe.type.TypeOfContract

internal fun TypeOfContract.toContractType(): ContractType = when (this) {
  TypeOfContract.NO_HOUSE,
  TypeOfContract.DK_HOUSE,
  TypeOfContract.SE_HOUSE,
  -> HOUSE

  TypeOfContract.DK_ACCIDENT,
  TypeOfContract.NO_ACCIDENT,
  TypeOfContract.SE_ACCIDENT,
  TypeOfContract.SE_ACCIDENT_STUDENT,
  TypeOfContract.DK_ACCIDENT_STUDENT,
  -> ACCIDENT

  TypeOfContract.DK_TRAVEL,
  TypeOfContract.DK_TRAVEL_STUDENT,
  TypeOfContract.NO_TRAVEL,
  TypeOfContract.NO_TRAVEL_STUDENT,
  TypeOfContract.NO_TRAVEL_YOUTH,
  -> TRAVEL

  TypeOfContract.DK_HOME_CONTENT_RENT,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.NO_HOME_CONTENT_RENT,
  TypeOfContract.SE_APARTMENT_RENT,
  TypeOfContract.SE_GROUP_APARTMENT_RENT,
  TypeOfContract.SE_QASA_LONG_TERM_RENTAL,
  TypeOfContract.SE_QASA_SHORT_TERM_RENTAL,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
  -> RENTAL

  TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.DK_HOME_CONTENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_OWN,
  TypeOfContract.SE_APARTMENT_BRF,
  TypeOfContract.SE_GROUP_APARTMENT_BRF,
  -> HOMEOWNER

  TypeOfContract.NO_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.SE_APARTMENT_STUDENT_BRF,
  TypeOfContract.SE_APARTMENT_STUDENT_RENT,
  -> STUDENT

  TypeOfContract.SE_CAR_FULL,
  TypeOfContract.SE_CAR_HALF,
  TypeOfContract.SE_CAR_TRAFFIC,
  -> CAR

  TypeOfContract.SE_CAT_BASIC,
  TypeOfContract.SE_CAT_PREMIUM,
  TypeOfContract.SE_CAT_STANDARD,
  -> CAT

  TypeOfContract.SE_DOG_BASIC,
  TypeOfContract.SE_DOG_PREMIUM,
  TypeOfContract.SE_DOG_STANDARD,
  -> DOG

  is TypeOfContract.UNKNOWN__ -> UNKNOWN
}

