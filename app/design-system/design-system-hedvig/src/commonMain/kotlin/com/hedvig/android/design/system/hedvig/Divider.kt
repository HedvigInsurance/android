package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens

@Composable
fun HorizontalDivider(
  modifier: Modifier = Modifier,
  thickness: Dp = DividerDefaults.thickness,
  color: Color = DividerDefaults.color,
) {
  Canvas(
    modifier
      .fillMaxWidth()
      .height(thickness),
  ) {
    drawLine(
      color = color,
      strokeWidth = thickness.toPx(),
      start = Offset(0f, thickness.toPx() / 2),
      end = Offset(size.width, thickness.toPx() / 2),
    )
  }
}

internal object DividerDefaults {
  val thickness: Dp = 1.dp

  val color: Color
    @Composable
    get() = HedvigTheme.colorScheme.fromToken(ColorSchemeKeyTokens.BorderSecondary)
}

@Composable
fun VerticalDivider(
  modifier: Modifier = Modifier,
  thickness: Dp = DividerDefaults.thickness,
  color: Color = DividerDefaults.color,
) = Canvas(
  modifier
    .fillMaxHeight()
    .width(thickness),
) {
  drawLine(
    color = color,
    strokeWidth = thickness.toPx(),
    start = Offset(thickness.toPx() / 2, 0f),
    end = Offset(thickness.toPx() / 2, size.height),
  )
}

@Preview
@Composable
private fun DividerPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
          HedvigText("First", Modifier.weight(1f))
          VerticalDivider()
          HedvigText("Second", Modifier.weight(1f).padding(start = 4.dp))
        }
        HorizontalDivider()
        HedvigText("Third")
      }
    }
  }
}
