package com.hedvig.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// colors https://github.com/HedvigInsurance/android/blob/e86158084061de59a9f1b6d71dda3b234057883d/app/src/main/res/values/colors.xml#L2
val hedvig_black = Color(0xFF121212)
val hedvig_off_white = Color(0xFFFAFAFA)
internal val lavender_300 = Color(0xFFC9ABF5)
val lavender_400 = Color(0xFFBE9BF3)
val lavender_600 = Color(0xFF875EC5)

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
