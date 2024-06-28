package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * This modifier is to be used with FigmaShapes if the color for border is translucent,
 * otherwise the color will turn out wrong due to Modifier.border inner bug
 */
fun Modifier.borderForTranslucentColor(width: Dp, color: Color, shape: Shape) =
  this.border(width = width, brush = Brush.linearGradient(listOf(color, color)), shape = shape)

fun Modifier.positionAwareImePadding() = composed {
  var consumePadding by remember { mutableStateOf(0) }
  with(LocalDensity.current) {
    onGloballyPositioned { coordinates ->
      val rootCoordinate = coordinates.findRootCoordinates()
      val bottom = coordinates.positionInWindow().y + coordinates.size.height

      consumePadding = (rootCoordinate.size.height - bottom).toInt()
    }
      .consumeWindowInsets(PaddingValues(bottom = consumePadding.toDp()))
      .imePadding()
  }
}
