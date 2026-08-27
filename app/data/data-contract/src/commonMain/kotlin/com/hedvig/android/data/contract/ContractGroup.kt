package com.hedvig.android.data.contract

import hedvigandroid.data_contract.generated.resources.Res
import hedvigandroid.data_contract.generated.resources.accident
import hedvigandroid.data_contract.generated.resources.car
import hedvigandroid.data_contract.generated.resources.cat
import hedvigandroid.data_contract.generated.resources.dog
import hedvigandroid.data_contract.generated.resources.home
import hedvigandroid.data_contract.generated.resources.homeowner
import hedvigandroid.data_contract.generated.resources.rental
import hedvigandroid.data_contract.generated.resources.safety
import hedvigandroid.data_contract.generated.resources.student
import hedvigandroid.data_contract.generated.resources.vacation
import hedvigandroid.data_contract.generated.resources.villa
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
  QASA_LANDLORD,
  PAYMENT_PROTECTION,
  UNKNOWN,
}

fun ContractGroup.gradientResource(): DrawableResource = when (this) {
  ContractGroup.HOMEOWNER -> Res.drawable.homeowner
  ContractGroup.HOUSE -> Res.drawable.villa
  ContractGroup.RENTAL -> Res.drawable.rental
  ContractGroup.STUDENT -> Res.drawable.student
  ContractGroup.ACCIDENT -> Res.drawable.accident
  ContractGroup.CAR -> Res.drawable.car
  ContractGroup.CAT -> Res.drawable.cat
  ContractGroup.DOG -> Res.drawable.dog
  ContractGroup.TRAVEL -> Res.drawable.homeowner
  ContractGroup.COUNTRY_HOME -> Res.drawable.vacation
  ContractGroup.UNKNOWN -> Res.drawable.home
  ContractGroup.QASA_LANDLORD -> Res.drawable.home
  ContractGroup.PAYMENT_PROTECTION -> Res.drawable.safety
}

fun ContractGroup.pillowResource(): DrawableResource = when (this) {
  ContractGroup.HOMEOWNER -> Res.drawable.homeowner
  ContractGroup.HOUSE -> Res.drawable.villa
  ContractGroup.RENTAL -> Res.drawable.rental
  ContractGroup.STUDENT -> Res.drawable.student
  ContractGroup.ACCIDENT -> Res.drawable.accident
  ContractGroup.CAR -> Res.drawable.car
  ContractGroup.CAT -> Res.drawable.cat
  ContractGroup.DOG -> Res.drawable.dog
  ContractGroup.TRAVEL -> Res.drawable.homeowner
  ContractGroup.COUNTRY_HOME -> Res.drawable.vacation
  ContractGroup.UNKNOWN -> Res.drawable.home
  ContractGroup.QASA_LANDLORD -> Res.drawable.home
  ContractGroup.PAYMENT_PROTECTION -> Res.drawable.safety
}

/**
 * A pillow illustration exposed for callers that need one without an owning contract, e.g. the home
 * add-on banners, which carry no imagery from the backend. The drawable resources are internal to
 * this module, so callers pick a pillow through this enum instead.
 */
enum class PillowType {
  CAR,
  VACATION,
}

fun PillowType.pillowResource(): DrawableResource = when (this) {
  PillowType.CAR -> Res.drawable.car
  PillowType.VACATION -> Res.drawable.vacation
}

fun String.toContractGroup(): ContractGroup = when (this) {
  "NO_HOUSE",
  "DK_HOUSE",
  "SE_HOUSE",
  "SE_HOUSE_BAS",
  "SE_HOUSE_MAX",
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
  "SE_APARTMENT_RENT_MAX",
  "SE_APARTMENT_RENT_BAS",
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
  "SE_APARTMENT_BRF_BAS",
  "SE_APARTMENT_BRF_MAX",
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

  "SE_QASA_LANDLORD" -> ContractGroup.QASA_LANDLORD

  "SE_PAYMENT_PROTECTION" -> ContractGroup.PAYMENT_PROTECTION

  else -> ContractGroup.UNKNOWN
}
