package com.hedvig.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// colors https://github.com/HedvigInsurance/android/blob/e86158084061de59a9f1b6d71dda3b234057883d/app/src/main/res/values/colors.xml#L2
internal val black = Color(0xFF000000)
internal val blur_white = Color(0xe6ffffff)
internal val white = Color(0xffffffff)
internal val purple = Color(0xff651eff)
internal val gray = Color(0xff9b9baa)
internal val semi_light_gray = Color(0xFFd7d7dc)
internal val light_gray = Color(0xFFe9ecef)
internal val off_white = Color(0xFFf9fafc)
internal val off_black = Color(0xFF414150)
internal val off_black_dark = Color(0xFF141033)
internal val dark_purple = Color(0xFF0F007A)
internal val green = Color(0xFF1BE9B6)
internal val dark_green = Color(0xFF009175)
internal val pink = Color(0xFFFF8A80)
internal val maroon = Color(0xFFAA0045)
internal val yellow = Color(0xFFF2C852)
internal val transparent = Color(0x00FFFFFF)
val button_background_dark = Color(0xFF333333)
internal val grey_inactive = Color(0xFF777777)
val hedvig_black = Color(0xFF121212)
val hedvig_off_white = Color(0xFFFAFAFA)
internal val hedvig_white = Color(0xFFFFFFFF)
val hedvig_light_gray = Color(0xFFEAEAEA)
internal val hedvig_dark_gray = Color(0xFF505050)
internal val hedvig_off_black = Color(0xFF1B1B1B)
val lavender_200 = Color(0xFFE7D6FF)
internal val lavender_300 = Color(0xFFC9ABF5)
val lavender_400 = Color(0xFFBE9BF3)
val lavender_600 = Color(0xFF875EC5)
val lavender_900 = Color(0xFF1C1724)
internal val text_color_primary_light = Color(0xAB121212)
internal val separator_light = Color(0x4A3C3C43)

val forever_orange_300 = Color(0xFFFCBA8D)
val forever_orange_500 = Color(0xFFFE9650)
val warning_light = Color(0xFFFAE098)
val warning_dark = Color(0xFFE3B945)

val hedvig_black12percent = hedvig_black.copy(alpha = 0.12f)

@Suppress("UnusedReceiverParameter")
val Colors.onWarning: Color
  get() = hedvig_black

@Suppress("UnusedReceiverParameter")
val Colors.warning: Color
  @Composable
  @ReadOnlyComposable
  get() = if (isSystemInDarkTheme()) {
    warning_dark
  } else {
    warning_light
  }

val Colors.separator: Color
  get() = if (isLight) {
    hedvig_black.copy(alpha = 0.12f)
  } else {
    hedvig_off_white.copy(alpha = 0.12f)
  }

val Colors.textColorLink: Color
  get() = if (isLight) {
    lavender_600
  } else {
    secondary
  }
