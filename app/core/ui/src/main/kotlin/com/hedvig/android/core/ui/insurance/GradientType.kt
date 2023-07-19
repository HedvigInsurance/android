package com.hedvig.android.core.ui.insurance

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
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

fun GradientType.toDrawable(context: Context): Drawable? = when (this) {
  GradientType.HOME -> ContextCompat.getDrawable(context, R.drawable.home)
  GradientType.ACCIDENT -> ContextCompat.getDrawable(context, R.drawable.accident)
  GradientType.HOUSE -> ContextCompat.getDrawable(context, R.drawable.house)
  GradientType.TRAVEL -> ContextCompat.getDrawable(context, R.drawable.travel)
  GradientType.CAR -> ContextCompat.getDrawable(context, R.drawable.car)
  GradientType.PET -> ContextCompat.getDrawable(context, R.drawable.pet)
  GradientType.UNKNOWN -> ContextCompat.getDrawable(context, R.drawable.unknown)
}
