package com.hedvig.app.util.extensions

import com.hedvig.android.core.ui.insurance.GradientType
import giraffe.type.TypeOfContract

fun TypeOfContract.gradient(): GradientType = when (this) {
  TypeOfContract.SE_ACCIDENT,
  TypeOfContract.SE_ACCIDENT_STUDENT,
  TypeOfContract.NO_ACCIDENT,
  TypeOfContract.DK_ACCIDENT_STUDENT,
  TypeOfContract.DK_ACCIDENT -> GradientType.ACCIDENT
  TypeOfContract.SE_APARTMENT_BRF,
  TypeOfContract.SE_APARTMENT_RENT,
  TypeOfContract.SE_APARTMENT_STUDENT_BRF,
  TypeOfContract.SE_APARTMENT_STUDENT_RENT,
  TypeOfContract.SE_GROUP_APARTMENT_RENT,
  TypeOfContract.SE_QASA_LONG_TERM_RENTAL,
  TypeOfContract.SE_QASA_SHORT_TERM_RENTAL,
  TypeOfContract.NO_HOME_CONTENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_RENT,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.DK_HOME_CONTENT_RENT,
  TypeOfContract.DK_HOME_CONTENT_OWN -> GradientType.HOME
  TypeOfContract.SE_HOUSE,
  TypeOfContract.NO_HOUSE,
  TypeOfContract.DK_HOUSE -> GradientType.HOUSE
  TypeOfContract.NO_TRAVEL,
  TypeOfContract.NO_TRAVEL_STUDENT,
  TypeOfContract.NO_TRAVEL_YOUTH,
  TypeOfContract.DK_TRAVEL_STUDENT,
  TypeOfContract.DK_TRAVEL -> GradientType.TRAVEL
  TypeOfContract.SE_CAR_FULL,
  TypeOfContract.SE_CAR_HALF,
  TypeOfContract.SE_CAR_TRAFFIC -> GradientType.CAR
  TypeOfContract.SE_CAT_BASIC,
  TypeOfContract.SE_CAT_PREMIUM,
  TypeOfContract.SE_CAT_STANDARD,
  TypeOfContract.SE_DOG_BASIC,
  TypeOfContract.SE_DOG_PREMIUM,
  TypeOfContract.SE_DOG_STANDARD -> GradientType.PET
  is TypeOfContract.UNKNOWN__ -> GradientType.UNKNOWN
}

fun TypeOfContract.canChangeCoInsured() = when (this) {
  TypeOfContract.SE_HOUSE,
  TypeOfContract.SE_APARTMENT_BRF,
  TypeOfContract.SE_APARTMENT_RENT,
  TypeOfContract.SE_APARTMENT_STUDENT_BRF,
  TypeOfContract.SE_APARTMENT_STUDENT_RENT,
  TypeOfContract.SE_ACCIDENT,
  TypeOfContract.SE_ACCIDENT_STUDENT,
  TypeOfContract.NO_HOUSE,
  TypeOfContract.NO_HOME_CONTENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_RENT,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.NO_TRAVEL,
  TypeOfContract.NO_TRAVEL_YOUTH,
  TypeOfContract.NO_TRAVEL_STUDENT,
  TypeOfContract.NO_ACCIDENT,
  TypeOfContract.DK_HOME_CONTENT_OWN,
  TypeOfContract.DK_HOME_CONTENT_RENT,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.DK_HOUSE,
  TypeOfContract.DK_ACCIDENT,
  TypeOfContract.DK_ACCIDENT_STUDENT,
  TypeOfContract.DK_TRAVEL,
  TypeOfContract.DK_TRAVEL_STUDENT -> true
  TypeOfContract.SE_CAR_TRAFFIC,
  TypeOfContract.SE_CAR_HALF,
  TypeOfContract.SE_CAR_FULL,
  TypeOfContract.SE_GROUP_APARTMENT_RENT,
  TypeOfContract.SE_QASA_SHORT_TERM_RENTAL,
  TypeOfContract.SE_QASA_LONG_TERM_RENTAL,
  TypeOfContract.SE_DOG_BASIC,
  TypeOfContract.SE_DOG_STANDARD,
  TypeOfContract.SE_DOG_PREMIUM,
  TypeOfContract.SE_CAT_BASIC,
  TypeOfContract.SE_CAT_STANDARD,
  TypeOfContract.SE_CAT_PREMIUM,
  is TypeOfContract.UNKNOWN__ -> false
}
