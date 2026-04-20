package com.hedvig.android.data.contract

import hedvigandroid.data_contract.generated.resources.Res
import hedvigandroid.data_contract.generated.resources.ic_pillow_accident
import hedvigandroid.data_contract.generated.resources.ic_pillow_car
import hedvigandroid.data_contract.generated.resources.ic_pillow_cat
import hedvigandroid.data_contract.generated.resources.ic_pillow_countryhome
import hedvigandroid.data_contract.generated.resources.ic_pillow_dog
import hedvigandroid.data_contract.generated.resources.ic_pillow_homeowner
import hedvigandroid.data_contract.generated.resources.ic_pillow_rental
import hedvigandroid.data_contract.generated.resources.ic_pillow_student
import hedvigandroid.data_contract.generated.resources.ic_pillow_villa
import org.jetbrains.compose.resources.DrawableResource

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
  COUNTRY_HOME,
  UNKNOWN,
}

fun ContractGroup.gradientResource(): DrawableResource = when (this) {
  ContractGroup.HOMEOWNER -> Res.drawable.ic_pillow_homeowner
  ContractGroup.HOUSE -> Res.drawable.ic_pillow_villa
  ContractGroup.RENTAL -> Res.drawable.ic_pillow_rental
  ContractGroup.STUDENT -> Res.drawable.ic_pillow_student
  ContractGroup.ACCIDENT -> Res.drawable.ic_pillow_accident
  ContractGroup.CAR -> Res.drawable.ic_pillow_car
  ContractGroup.CAT -> Res.drawable.ic_pillow_cat
  ContractGroup.DOG -> Res.drawable.ic_pillow_dog
  ContractGroup.TRAVEL -> Res.drawable.ic_pillow_homeowner
  ContractGroup.COUNTRY_HOME -> Res.drawable.ic_pillow_countryhome
  ContractGroup.UNKNOWN -> Res.drawable.ic_pillow_homeowner
}

fun ContractGroup.pillowResource(): DrawableResource = when (this) {
  ContractGroup.HOMEOWNER -> Res.drawable.ic_pillow_homeowner
  ContractGroup.HOUSE -> Res.drawable.ic_pillow_villa
  ContractGroup.RENTAL -> Res.drawable.ic_pillow_rental
  ContractGroup.STUDENT -> Res.drawable.ic_pillow_student
  ContractGroup.ACCIDENT -> Res.drawable.ic_pillow_accident
  ContractGroup.CAR -> Res.drawable.ic_pillow_car
  ContractGroup.CAT -> Res.drawable.ic_pillow_cat
  ContractGroup.DOG -> Res.drawable.ic_pillow_dog
  ContractGroup.TRAVEL -> Res.drawable.ic_pillow_homeowner
  ContractGroup.COUNTRY_HOME -> Res.drawable.ic_pillow_countryhome
  ContractGroup.UNKNOWN -> Res.drawable.ic_pillow_homeowner
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
  "SE_CAR_DECOMMISSIONED",
  -> ContractGroup.CAR

  "SE_CAT_BASIC",
  "SE_CAT_PREMIUM",
  "SE_CAT_STANDARD",
  -> ContractGroup.CAT

  "SE_DOG_BASIC",
  "SE_DOG_PREMIUM",
  "SE_DOG_STANDARD",
  -> ContractGroup.DOG

  "SE_VACATION_HOME_BAS",
  "SE_VACATION_HOME_STANDARD",
  -> ContractGroup.COUNTRY_HOME

  else -> ContractGroup.UNKNOWN
}
