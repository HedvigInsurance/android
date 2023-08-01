package com.hedvig.android.core.ui.insurance

import com.hedvig.android.core.ui.R

enum class GradientType {
  HOME,
  ACCIDENT,
  HOUSE,
  TRAVEL,
  CAR,
  PET,
  UNKNOWN,
}

fun GradientType.toDrawableRes(): Int = when (this) {
  GradientType.HOME -> R.drawable.gradient_homeowner
  GradientType.ACCIDENT -> R.drawable.gradient_accident
  GradientType.HOUSE -> R.drawable.gradient_villa
  GradientType.TRAVEL -> R.drawable.gradient_homeowner
  GradientType.CAR -> R.drawable.gradient_car
  GradientType.PET -> R.drawable.gradient_dog
  GradientType.UNKNOWN -> R.drawable.gradient_homeowner
}
