package com.hedvig.android.design.system.hedvig

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens

fun Modifier.horizontalDivider(
  position: DividerPosition,
  show: Boolean = true,
  horizontalPadding: Dp = 0.dp,
  thickness: Dp = DividerDefaults.thickness,
  color: Color? = null,
): Modifier = this then HorizontalDividerElement(position, show, horizontalPadding, thickness, color)

enum class DividerPosition {
  Top,
  Bottom,
}

@SuppressLint("ModifierNodeInspectableProperties")
private data class HorizontalDividerElement(
  val position: DividerPosition,
  val show: Boolean,
  val horizontalPadding: Dp,
  val thickness: Dp,
  val color: Color?,
) : ModifierNodeElement<HorizontalDividerNode>() {
  override fun create(): HorizontalDividerNode = HorizontalDividerNode(
    position = position,
    show = show,
    horizontalPadding = horizontalPadding,
    thickness = thickness,
    color = color,
  )

  override fun update(node: HorizontalDividerNode) {
    node.position = position
    node.show = show
    node.horizontalPadding = horizontalPadding
    node.thickness = thickness
    node.color = color
  }
}

private class HorizontalDividerNode(
  var position: DividerPosition,
  var show: Boolean,
  var horizontalPadding: Dp,
  var thickness: Dp,
  var color: Color?,
) : Modifier.Node(),
  DrawModifierNode,
  CompositionLocalConsumerModifierNode {
  override fun ContentDrawScope.draw() {
    drawContent()
    if (!show) return@draw
    val borderColor = color ?: currentValueOf(LocalColorScheme).fromToken(ColorSchemeKeyTokens.BorderSecondary)
    val thickness = thickness.toPx()
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
