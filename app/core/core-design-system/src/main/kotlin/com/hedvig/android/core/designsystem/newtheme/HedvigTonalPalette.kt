package com.hedvig.android.core.designsystem.newtheme

import androidx.compose.ui.graphics.Color

internal val hedvigTonalPalette: HedvigTonalPalette = HedvigTonalPaletteImpl(
  // The neutral tonal range from the generated dynamic color palette.
  greyscale0 = greyscale_0,
  greyscale10 = greyscale_10,
  greyscale50 = greyscale_50,
  greyscale100 = greyscale_100,
  greyscale200 = greyscale_200,
  greyscale300 = greyscale_300,
  greyscale400 = greyscale_400,
  greyscale500 = greyscale_500,
  greyscale600 = greyscale_600,
  greyscale700 = greyscale_700,
  greyscale800 = greyscale_800,
  greyscale900 = greyscale_900,
  greyscale1000 = greyscale_1000,
)

internal interface HedvigTonalPalette {
  val greyscale0: Color
  val greyscale10: Color
  val greyscale50: Color
  val greyscale100: Color
  val greyscale200: Color
  val greyscale300: Color
  val greyscale400: Color
  val greyscale500: Color
  val greyscale600: Color
  val greyscale700: Color
  val greyscale800: Color
  val greyscale900: Color
  val greyscale1000: Color
}

private class HedvigTonalPaletteImpl(
  override val greyscale0: Color,
  override val greyscale10: Color,
  override val greyscale50: Color,
  override val greyscale100: Color,
  override val greyscale200: Color,
  override val greyscale300: Color,
  override val greyscale400: Color,
  override val greyscale500: Color,
  override val greyscale600: Color,
  override val greyscale700: Color,
  override val greyscale800: Color,
  override val greyscale900: Color,
  override val greyscale1000: Color,
) : HedvigTonalPalette {
  // The neutral tonal range from the generated dynamic color palette.
  // Ordered from the lightest shade [neutral100] to the darkest shade [neutral0].
  // Kept for reference if we need to know the `neutralX` notation of those
  @Suppress("unused")
  private val neutral100: Color = greyscale0

  @Suppress("unused")
  private val neutral99: Color = greyscale10

  @Suppress("unused")
  private val neutral95: Color = greyscale50

  @Suppress("unused")
  private val neutral90: Color = greyscale100

  @Suppress("unused")
  private val neutral80: Color = greyscale200

  @Suppress("unused")
  private val neutral70: Color = greyscale300

  @Suppress("unused")
  private val neutral60: Color = greyscale400

  @Suppress("unused")
  private val neutral50: Color = greyscale500

  @Suppress("unused")
  private val neutral40: Color = greyscale600

  @Suppress("unused")
  private val neutral30: Color = greyscale700

  @Suppress("unused")
  private val neutral20: Color = greyscale800

  @Suppress("unused")
  private val neutral10: Color = greyscale900

  @Suppress("unused")
  private val neutral0: Color = greyscale1000
}
