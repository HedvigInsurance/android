package com.hedvig.android.core.designsystem.newtheme

import androidx.compose.ui.graphics.Color

internal val hedvigTonalPalette: HedvigTonalPalette = HedvigTonalPalette(
  // The neutral tonal range from the generated dynamic color palette.
  neutral100 = greyscale_0,
  neutral99 = greyscale_10,
  neutral95 = greyscale_50,
  neutral90 = greyscale_100,
  neutral80 = greyscale_200,
  neutral70 = greyscale_300,
  neutral60 = greyscale_400,
  neutral50 = greyscale_500,
  neutral40 = greyscale_600,
  neutral30 = greyscale_700,
  neutral20 = greyscale_800,
  neutral10 = greyscale_900,
  neutral0 = greyscale_1000,
)

internal class HedvigTonalPalette(
  // The neutral tonal range from the generated dynamic color palette.
  // Ordered from the lightest shade [neutral100] to the darkest shade [neutral0].
  val neutral100: Color,
  val neutral99: Color,
  val neutral95: Color,
  val neutral90: Color,
  val neutral80: Color,
  val neutral70: Color,
  val neutral60: Color,
  val neutral50: Color,
  val neutral40: Color,
  val neutral30: Color,
  val neutral20: Color,
  val neutral10: Color,
  val neutral0: Color,
)
