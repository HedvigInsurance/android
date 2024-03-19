package com.hedvig.android.data.contract

import kotlinx.serialization.Serializable

@Serializable
enum class ContractGroup {
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

fun String.toContractGroup(): ContractGroup = when (this) {
  "NO_HOUSE",
  "DK_HOUSE",
  "SE_HOUSE",
  -> ContractGroup.HOUSE

  "DK_ACCIDENT",
  "NO_ACCIDENT",
  "SE_ACCIDENT",
  "SE_ACCIDENT_STUDENT",
  "DK_ACCIDENT_STUDENT",
  -> ContractGroup.ACCIDENT

  "DK_TRAVEL",
  "DK_TRAVEL_STUDENT",
  "NO_TRAVEL",
  "NO_TRAVEL_STUDENT",
  "NO_TRAVEL_YOUTH",
  -> ContractGroup.TRAVEL

  "DK_HOME_CONTENT_RENT",
  "DK_HOME_CONTENT_STUDENT_RENT",
  "NO_HOME_CONTENT_RENT",
  "SE_APARTMENT_RENT",
  "SE_GROUP_APARTMENT_RENT",
  "SE_QASA_LONG_TERM_RENTAL",
  "SE_QASA_SHORT_TERM_RENTAL",
  "NO_HOME_CONTENT_YOUTH_RENT",
  -> ContractGroup.RENTAL

  "NO_HOME_CONTENT_YOUTH_OWN",
  "DK_HOME_CONTENT_STUDENT_OWN",
  "DK_HOME_CONTENT_OWN",
  "NO_HOME_CONTENT_OWN",
  "SE_APARTMENT_BRF",
  "SE_GROUP_APARTMENT_BRF",
  -> ContractGroup.HOMEOWNER

  "NO_HOME_CONTENT_STUDENT_OWN",
  "NO_HOME_CONTENT_STUDENT_RENT",
  "SE_APARTMENT_STUDENT_BRF",
  "SE_APARTMENT_STUDENT_RENT",
  -> ContractGroup.STUDENT

  "SE_CAR_FULL",
  "SE_CAR_HALF",
  "SE_CAR_TRAFFIC",
  "SE_CAR_TRIAL_HALF",
  "SE_CAR_TRIAL_FULL",
  -> ContractGroup.CAR

  "SE_CAT_BASIC",
  "SE_CAT_PREMIUM",
  "SE_CAT_STANDARD",
  -> ContractGroup.CAT

  "SE_DOG_BASIC",
  "SE_DOG_PREMIUM",
  "SE_DOG_STANDARD",
  -> ContractGroup.DOG

  else -> ContractGroup.UNKNOWN
}
