package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens

@Composable
fun HorizontalDivider(
  modifier: Modifier = Modifier,
  thickness: Dp = DividerDefaults.thickness,
  color: Color = DividerDefaults.color,
) = Canvas(modifier.fillMaxWidth().height(thickness)) {
  drawLine(
    color = color,
    strokeWidth = thickness.toPx(),
    start = Offset(0f, thickness.toPx() / 2),
    end = Offset(size.width, thickness.toPx() / 2),
  )
}

internal object DividerDefaults {
  val thickness: Dp = 1.dp

  val color: Color
    @Composable
    get() = HedvigTheme.colorScheme.fromToken(ColorSchemeKeyTokens.BorderSecondary)
}
