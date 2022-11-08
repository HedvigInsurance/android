package com.hedvig.app.util

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.graphics.ColorUtils
import com.hedvig.android.apollo.graphql.type.HedvigColor
import com.hedvig.app.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@ColorInt
fun boundedColorLerp(@ColorInt from: Int, @ColorInt to: Int, amount: Float): Int {
  val fromAlpha = Color.alpha(from)
  val fromRed = Color.red(from)
  val fromGreen = Color.green(from)
  val fromBlue = Color.blue(from)

  val toAlpha = Color.alpha(to)
  val toRed = Color.red(to)
  val toGreen = Color.green(to)
  val toBlue = Color.blue(to)

  val resultAlpha = boundedLerp(fromAlpha, toAlpha, amount)
  val resultRed = boundedLerp(fromRed, toRed, amount)
  val resultGreen = boundedLerp(fromGreen, toGreen, amount)
  val resultBlue = boundedLerp(fromBlue, toBlue, amount)

  return Color.argb(resultAlpha, resultRed, resultGreen, resultBlue)
}

@ColorRes
fun HedvigColor.mappedColor(): Int = when (this) {
  HedvigColor.DarkPurple -> R.color.dark_purple
  HedvigColor.LightGray -> R.color.light_gray
  HedvigColor.OffWhite -> R.color.off_white
  HedvigColor.DarkGray -> R.color.gray
  HedvigColor.Purple -> R.color.purple
  HedvigColor.White -> R.color.white
  HedvigColor.OffBlack -> R.color.off_black
  HedvigColor.Black -> R.color.black
  HedvigColor.Turquoise -> R.color.green
  HedvigColor.Pink -> R.color.pink
  HedvigColor.BlackPurple -> R.color.off_black_dark
  HedvigColor.Yellow -> R.color.yellow
  HedvigColor.UNKNOWN__ -> R.color.purple
}

val colorArray = arrayOf(
  R.color.purple,
  R.color.dark_purple,
  R.color.green,
  R.color.dark_green,
  R.color.pink,
  R.color.maroon,
  R.color.yellow,
)

@ColorRes
fun hashColor(obj: Any) = colorArray[abs(obj.hashCode()) % colorArray.size]

@ColorInt
fun lightenColor(@ColorInt color: Int, factor: Float): Int {
  val hsl = FloatArray(3)
  ColorUtils.colorToHSL(color, hsl)

  hsl[2] += factor
  hsl[2] = max(0f, min(hsl[2], 1f))

  return ColorUtils.HSLToColor(hsl)
}

@ColorInt
fun darkenColor(@ColorInt color: Int, factor: Float): Int {
  val hsl = FloatArray(3)
  ColorUtils.colorToHSL(color, hsl)
  hsl[2] -= factor
  hsl[2] = max(0f, min(hsl[2], 1f))

  return ColorUtils.HSLToColor(hsl)
}

enum class LightClass {
  DARK,
  LIGHT
}

fun getLightness(@ColorInt color: Int) = if (ColorUtils.calculateLuminance(color) < 0.5) {
  LightClass.DARK
} else {
  LightClass.LIGHT
}
