package com.hedvig.android.core.icons.hedvig.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * A notification circle attached to the top right taking up ~35% of the available size.
 * https://www.figma.com/file/qUhLjrKl98PAzHov9ilaDH/Hedvig-UI-Kit?type=design&node-id=3813%3A19134&mode=design&t=V1DM52RqO3kDFMUq-1
 */
fun Modifier.notificationCircle(horizontalOffset: Dp, showNotification: Boolean = true) = this.drawWithContent {
  drawContent()
  if (showNotification) {
    // The red circle takes up ~34% of the icon's size
    val circleDiameter = size.minDimension * 0.34375f
    val circleRadius = circleDiameter / 2
    drawCircle(
      color = Color(0xFFFF513A),
      radius = circleRadius,
      center = Offset(size.width - circleRadius + horizontalOffset.toPx(), circleRadius),
    )
  }
}
