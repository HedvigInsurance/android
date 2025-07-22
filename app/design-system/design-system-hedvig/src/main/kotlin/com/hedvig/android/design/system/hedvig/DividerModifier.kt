package com.hedvig.android.design.system.hedvig

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens

fun Modifier.horizontalDivider(position: DividerPosition, horizontalPadding: Dp = 0.dp): Modifier =
  this then HorizontalDividerElement(position, horizontalPadding)

enum class DividerPosition {
  Top,
  Bottom,
}

@SuppressLint("ModifierNodeInspectableProperties")
private data class HorizontalDividerElement(
  val position: DividerPosition,
  val horizontalPadding: Dp,
) : ModifierNodeElement<HorizontalDividerNode>() {
  override fun create(): HorizontalDividerNode = HorizontalDividerNode(position, horizontalPadding)

  override fun update(node: HorizontalDividerNode) {
    node.position = position
    node.horizontalPadding = horizontalPadding
  }
}

private class HorizontalDividerNode(
  var position: DividerPosition,
  var horizontalPadding: Dp,
) : Modifier.Node(),
  DrawModifierNode,
  CompositionLocalConsumerModifierNode {
  override fun ContentDrawScope.draw() {
    drawContent()
    val borderColor = currentValueOf(LocalColorScheme).fromToken(ColorSchemeKeyTokens.BorderSecondary)
    val thickness = DividerDefaults.thickness.toPx()
    val yOffset = when (position) {
      DividerPosition.Top -> 0f + thickness / 2
      DividerPosition.Bottom -> size.height - thickness / 2
    }
    drawLine(
      color = borderColor,
      strokeWidth = thickness,
      start = Offset(horizontalPadding.toPx(), yOffset),
      end = Offset(size.width - horizontalPadding.toPx(), yOffset),
    )
  }
}
