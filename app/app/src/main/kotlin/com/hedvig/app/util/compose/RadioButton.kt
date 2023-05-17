package com.hedvig.app.util.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.RadioButtonColors
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stripped down version of [androidx.compose.material.RadioButton] which allows for a different size.
 */
@Composable
fun RadioButton(
  selected: Boolean,
  size: Dp,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: RadioButtonColors = RadioButtonDefaults.colors(),
) {
  val radioRadius = remember(size) { size / 2 }
  val radioButtonDotSize = remember(size) { size - 8.dp }
  val dotRadius = animateDpAsState(
    targetValue = if (selected) radioButtonDotSize / 2 else 0.dp,
    animationSpec = tween(durationMillis = RadioAnimationDuration),
    label = "dotRadius",
  )
  val radioColor = colors.radioColor(enabled, selected)
  Canvas(
    modifier
      .wrapContentSize(Alignment.Center)
      .padding(RadioButtonPadding)
      .requiredSize(size),
  ) {
    val strokeWidth = RadioStrokeWidth.toPx()
    drawCircle(
      radioColor.value,
      radioRadius.toPx() - strokeWidth / 2,
      style = Stroke(strokeWidth),
    )
    if (dotRadius.value > 0.dp) {
      drawCircle(radioColor.value, dotRadius.value.toPx() - strokeWidth / 2, style = Fill)
    }
  }
}

private const val RadioAnimationDuration = 100

private val RadioButtonPadding = 2.dp
private val RadioStrokeWidth = 2.dp
