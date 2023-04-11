package com.hedvig.android.core.ui.insurance

import android.content.Context
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

fun GradientType.toDrawable(context: Context) = when (this) {
  GradientType.HOME -> context.getDrawable(R.drawable.home)
  GradientType.ACCIDENT -> context.getDrawable(R.drawable.accident)
  GradientType.HOUSE -> context.getDrawable(R.drawable.house)
  GradientType.TRAVEL -> context.getDrawable(R.drawable.travel)
  GradientType.CAR -> context.getDrawable(R.drawable.car)
  GradientType.PET -> context.getDrawable(R.drawable.pet)
  GradientType.UNKNOWN -> context.getDrawable(R.drawable.unknown)
}
