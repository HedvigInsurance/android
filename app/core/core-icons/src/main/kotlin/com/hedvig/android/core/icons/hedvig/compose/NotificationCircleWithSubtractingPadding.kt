package com.hedvig.android.core.icons.hedvig.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A notification circle, with a little padding around it which subtracts what is shown on the original composable by
 * clearing the pixels there and making it transparent. It is attached to the top right of the available size.
 * https://www.figma.com/file/qUhLjrKl98PAzHov9ilaDH/Hedvig-UI-Kit?type=design&node-id=3813%3A19134&mode=design&t=V1DM52RqO3kDFMUq-1
 */
fun Modifier.notificationCircleWithSubtractingPadding(showNotification: Boolean = true): Modifier {
  return this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
      drawContent()
      if (showNotification) {
        // The red circle takes up ~30% of the icon's size
        val redCircleDiameter = size.minDimension * 0.291666666666667f
        val redCircleRadius = redCircleDiameter / 2
        // The padding around it is ~42% bigger than the red circle
        val paddingCircleDiameter = redCircleDiameter + (redCircleDiameter * 0.428571428571429f)
        val paddingCircleRadius = paddingCircleDiameter / 2
        // Circle is offset by 5% of the total size of the icon.
        val fivePercentOffset = size.minDimension / 20
        val circleOffsetWithPaddingConsidered = Offset(
          x = size.width - paddingCircleRadius + fivePercentOffset,
          y = paddingCircleRadius - fivePercentOffset,
        )
        drawCircle(
          color = Color.Transparent,
          radius = paddingCircleRadius,
          center = circleOffsetWithPaddingConsidered,
          blendMode = BlendMode.Clear,
        )
        drawCircle(
          color = Color(0xFFFF513A),
          radius = redCircleRadius,
          center = circleOffsetWithPaddingConsidered,
        )
      }
    }
}
