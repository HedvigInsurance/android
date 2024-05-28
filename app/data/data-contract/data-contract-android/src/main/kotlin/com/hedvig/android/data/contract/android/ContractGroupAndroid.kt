package com.hedvig.android.data.contract.android

import com.hedvig.android.data.contract.ContractGroup

fun ContractGroup.toDrawableRes(): Int = when (this) {
  ContractGroup.HOMEOWNER -> R.drawable.gradient_homeowner
  ContractGroup.HOUSE -> R.drawable.gradient_villa
  ContractGroup.RENTAL -> R.drawable.gradient_rental
  ContractGroup.STUDENT -> R.drawable.gradient_student
  ContractGroup.ACCIDENT -> R.drawable.gradient_accident
  ContractGroup.CAR -> R.drawable.gradient_car
  ContractGroup.CAT -> R.drawable.gradient_cat
  ContractGroup.DOG -> R.drawable.gradient_dog
  ContractGroup.TRAVEL -> R.drawable.gradient_homeowner
  ContractGroup.UNKNOWN -> R.drawable.gradient_homeowner
}

fun ContractGroup.toPillow(): Int = when (this) {
  ContractGroup.HOMEOWNER -> R.drawable.ic_pillow_homeowner
  ContractGroup.HOUSE -> R.drawable.ic_pillow_villa
  ContractGroup.RENTAL -> R.drawable.ic_pillow_rental
  ContractGroup.STUDENT -> R.drawable.ic_pillow_student
  ContractGroup.ACCIDENT -> R.drawable.ic_pillow_accident
  ContractGroup.CAR -> R.drawable.ic_pillow_car
  ContractGroup.CAT -> R.drawable.ic_pillow_cat
  ContractGroup.DOG -> R.drawable.ic_pillow_dog
  ContractGroup.TRAVEL -> R.drawable.ic_pillow_homeowner
  ContractGroup.UNKNOWN -> R.drawable.ic_pillow_homeowner
}
