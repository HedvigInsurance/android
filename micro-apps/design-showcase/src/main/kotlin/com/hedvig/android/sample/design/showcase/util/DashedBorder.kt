package com.hedvig.android.sample.design.showcase.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal fun Modifier.dashedBorder(
  color: Color,
  shape: Shape,
  strokeWidth: Dp = 2.dp,
  dashWidth: Dp = 8.dp,
  gapWidth: Dp = 4.dp,
  cap: StrokeCap = StrokeCap.Round,
) = this.drawWithCache {
  val outline = shape.createOutline(size, layoutDirection, this)
  val path = Path().apply {
    addOutline(outline)
  }
  val stroke = Stroke(
    cap = cap,
    width = strokeWidth.toPx(),
    pathEffect = PathEffect.dashPathEffect(
      intervals = floatArrayOf(dashWidth.toPx(), gapWidth.toPx()),
      phase = 0f,
    ),
  )

  onDrawWithContent {
    drawContent()
    drawPath(
      path = path,
      style = stroke,
      color = color,
    )
  }
}
