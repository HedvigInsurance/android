package com.hedvig.android.core.ui.insurance

import com.hedvig.android.core.ui.R

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

fun ContractType.toDrawableRes(): Int = when (this) {
  ContractType.HOMEOWNER -> R.drawable.gradient_homeowner
  ContractType.HOUSE -> R.drawable.gradient_villa
  ContractType.RENTAL -> R.drawable.gradient_rental
  ContractType.STUDENT -> R.drawable.gradient_student
  ContractType.ACCIDENT -> R.drawable.gradient_accident
  ContractType.CAR -> R.drawable.gradient_car
  ContractType.CAT -> R.drawable.gradient_cat
  ContractType.DOG -> R.drawable.gradient_dog
  ContractType.TRAVEL -> R.drawable.gradient_homeowner
  ContractType.UNKNOWN -> R.drawable.gradient_homeowner
}

fun ContractType.toPillow(): Int = when (this) {
  ContractType.HOMEOWNER -> R.drawable.ic_pillow_homeowner
  ContractType.HOUSE -> R.drawable.ic_pillow_villa
  ContractType.RENTAL -> R.drawable.ic_pillow_rental
  ContractType.STUDENT -> R.drawable.ic_pillow_student
  ContractType.ACCIDENT -> R.drawable.ic_pillow_accident
  ContractType.CAR -> R.drawable.ic_pillow_car
  ContractType.CAT -> R.drawable.ic_pillow_cat
  ContractType.DOG -> R.drawable.ic_pillow_dog
  ContractType.TRAVEL -> R.drawable.ic_pillow_homeowner
  ContractType.UNKNOWN -> R.drawable.ic_pillow_homeowner
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
