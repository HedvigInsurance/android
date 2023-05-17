package com.hedvig.android.core.designsystem.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun hedvigContentColorFor(backgroundColor: Color): Color {
  return when (backgroundColor) {
    lavender_200 -> hedvig_black
    lavender_400 -> hedvig_black
    forever_orange_500 -> hedvig_black
    warning_light -> hedvig_black
    warning_dark -> hedvig_black
    Color.Transparent -> contentColorFor(MaterialTheme.colors.background)
    else -> contentColorFor(backgroundColor)
  }
}
