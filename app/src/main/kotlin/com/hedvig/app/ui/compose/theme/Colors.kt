package com.hedvig.app.ui.compose.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.hedvig.app.R

@Composable
fun hedvigContentColorFor(backgroundColor: Color): Color {
  return when (backgroundColor) {
    colorResource(R.color.lavender_200) -> colorResource(R.color.hedvig_black)
    colorResource(R.color.lavender_400) -> colorResource(R.color.hedvig_black)
    colorResource(R.color.forever_orange_500) -> colorResource(R.color.hedvig_black)
    colorResource(R.color.colorWarning) -> colorResource(R.color.hedvig_black)
    Color.Transparent -> contentColorFor(MaterialTheme.colors.background)
    else -> contentColorFor(backgroundColor)
  }
}

@Suppress("unused")
val Colors.warning: Color
  @Composable
  @ReadOnlyComposable
  get() = colorResource(R.color.colorWarning)
