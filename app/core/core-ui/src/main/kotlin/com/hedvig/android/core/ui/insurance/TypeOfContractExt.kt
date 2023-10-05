package com.hedvig.android.core.ui.insurance

import giraffe.type.TypeOfContract

fun TypeOfContract.toContractType(): ContractType = when (this) {
  TypeOfContract.NO_HOUSE,
  TypeOfContract.DK_HOUSE,
  TypeOfContract.SE_HOUSE,
  -> ContractType.HOUSE

  TypeOfContract.DK_ACCIDENT,
  TypeOfContract.NO_ACCIDENT,
  TypeOfContract.SE_ACCIDENT,
  TypeOfContract.SE_ACCIDENT_STUDENT,
  TypeOfContract.DK_ACCIDENT_STUDENT,
  -> ContractType.ACCIDENT

  TypeOfContract.DK_TRAVEL,
  TypeOfContract.DK_TRAVEL_STUDENT,
  TypeOfContract.NO_TRAVEL,
  TypeOfContract.NO_TRAVEL_STUDENT,
  TypeOfContract.NO_TRAVEL_YOUTH,
  -> ContractType.TRAVEL

  TypeOfContract.DK_HOME_CONTENT_RENT,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.NO_HOME_CONTENT_RENT,
  TypeOfContract.SE_APARTMENT_RENT,
  TypeOfContract.SE_GROUP_APARTMENT_RENT,
  TypeOfContract.SE_QASA_LONG_TERM_RENTAL,
  TypeOfContract.SE_QASA_SHORT_TERM_RENTAL,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
  -> ContractType.RENTAL

  TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.DK_HOME_CONTENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_OWN,
  TypeOfContract.SE_APARTMENT_BRF,
  TypeOfContract.SE_GROUP_APARTMENT_BRF,
  -> ContractType.HOMEOWNER

  TypeOfContract.NO_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.SE_APARTMENT_STUDENT_BRF,
  TypeOfContract.SE_APARTMENT_STUDENT_RENT,
  -> ContractType.STUDENT

  TypeOfContract.SE_CAR_FULL,
  TypeOfContract.SE_CAR_HALF,
  TypeOfContract.SE_CAR_TRAFFIC,
  -> ContractType.CAR

  TypeOfContract.SE_CAT_BASIC,
  TypeOfContract.SE_CAT_PREMIUM,
  TypeOfContract.SE_CAT_STANDARD,
  -> ContractType.CAT

  TypeOfContract.SE_DOG_BASIC,
  TypeOfContract.SE_DOG_PREMIUM,
  TypeOfContract.SE_DOG_STANDARD,
  -> ContractType.DOG

  is TypeOfContract.UNKNOWN__ -> ContractType.UNKNOWN
}

fun String.toContractType(): ContractType = when (this) {
  "NO_HOUSE",
  "DK_HOUSE",
  "SE_HOUSE",
  -> ContractType.HOUSE

  "DK_ACCIDENT",
  "NO_ACCIDENT",
  "SE_ACCIDENT",
  "SE_ACCIDENT_STUDENT",
  "DK_ACCIDENT_STUDENT",
  -> ContractType.ACCIDENT

  "DK_TRAVEL",
  "DK_TRAVEL_STUDENT",
  "NO_TRAVEL",
  "NO_TRAVEL_STUDENT",
  "NO_TRAVEL_YOUTH",
  -> ContractType.TRAVEL

  "DK_HOME_CONTENT_RENT",
  "DK_HOME_CONTENT_STUDENT_RENT",
  "NO_HOME_CONTENT_RENT",
  "SE_APARTMENT_RENT",
  "SE_GROUP_APARTMENT_RENT",
  "SE_QASA_LONG_TERM_RENTAL",
  "SE_QASA_SHORT_TERM_RENTAL",
  "NO_HOME_CONTENT_YOUTH_RENT",
  -> ContractType.RENTAL

  "NO_HOME_CONTENT_YOUTH_OWN",
  "DK_HOME_CONTENT_STUDENT_OWN",
  "DK_HOME_CONTENT_OWN",
  "NO_HOME_CONTENT_OWN",
  "SE_APARTMENT_BRF",
  "SE_GROUP_APARTMENT_BRF",
  -> ContractType.HOMEOWNER

  "NO_HOME_CONTENT_STUDENT_OWN",
  "NO_HOME_CONTENT_STUDENT_RENT",
  "SE_APARTMENT_STUDENT_BRF",
  "SE_APARTMENT_STUDENT_RENT",
  -> ContractType.STUDENT

  "SE_CAR_FULL",
  "SE_CAR_HALF",
  "SE_CAR_TRAFFIC",
  -> ContractType.CAR

  "SE_CAT_BASIC",
  "SE_CAT_PREMIUM",
  "SE_CAT_STANDARD",
  -> ContractType.CAT

  "SE_DOG_BASIC",
  "SE_DOG_PREMIUM",
  "SE_DOG_STANDARD",
  -> ContractType.DOG

  else -> ContractType.UNKNOWN
}
