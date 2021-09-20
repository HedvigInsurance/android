package com.hedvig.app.feature.offer.ui

import android.content.Context
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable

enum class GradientType {
    FALL_SUNSET,
    SPRING_FOG,
    SUMMER_SKY,
    UNKNOWN,
}

fun GradientType.toDrawable(context: Context) = when (this) {
    GradientType.FALL_SUNSET -> context.compatDrawable(R.drawable.gradient_fall_sunset)
    GradientType.SPRING_FOG -> context.compatDrawable(R.drawable.gradient_spring_fog)
    GradientType.SUMMER_SKY -> context.compatDrawable(R.drawable.gradient_summer_sky)
    GradientType.UNKNOWN -> context.compatDrawable(R.drawable.gradient_spring_fog)
}

