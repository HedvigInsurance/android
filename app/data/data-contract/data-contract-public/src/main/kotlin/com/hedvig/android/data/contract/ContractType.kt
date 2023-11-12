package com.hedvig.android.data.contract

enum class ContractType {
  HOMEOWNER,
  RENTAL,
  ACCIDENT,
  HOUSE,
  TRAVEL,
  CAR,
  CAT,
  DOG,
  STUDENT,
  UNKNOWN,
}

fun ContractType.canChangeCoInsured() = when (this) {
  ContractType.HOMEOWNER,
  ContractType.RENTAL,
  ContractType.ACCIDENT,
  ContractType.HOUSE,
  ContractType.STUDENT,
  ContractType.TRAVEL,
  -> true
  ContractType.CAR,
  ContractType.CAT,
  ContractType.DOG,
  ContractType.UNKNOWN,
  -> false
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
