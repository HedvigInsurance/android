package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import kotlin.math.roundToInt

/**
 * Source:
 * https://cs.android.com/androidx/platform/frameworks/support/+/710392834c2317072387b04235d2ad02012abaf8:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/InteractiveComponentSize.kt;l=52
 */
@Stable
fun Modifier.minimumInteractiveComponentSize(): Modifier = this then MinimumInteractiveModifier

internal object MinimumInteractiveModifier : ModifierNodeElement<MinimumInteractiveModifierNode>() {
  override fun create(): MinimumInteractiveModifierNode = MinimumInteractiveModifierNode()

  override fun update(node: MinimumInteractiveModifierNode) {}

  override fun InspectorInfo.inspectableProperties() {
    name = "minimumInteractiveComponentSize"
    properties["README"] = "Reserves at least 48.dp in size to disambiguate touch " +
      "interactions if the element would measure smaller"
  }

  override fun hashCode(): Int = System.identityHashCode(this)

  override fun equals(other: Any?) = (other === this)
}

internal class MinimumInteractiveModifierNode :
  Modifier.Node(),
  CompositionLocalConsumerModifierNode,
  LayoutModifierNode {
  override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
    val size = currentValueOf(LocalMinimumInteractiveComponentSize).coerceAtLeast(0.dp)
    val placeable = measurable.measure(constraints)
    val enforcement = isAttached && (size.isSpecified && size > 0.dp)

    val sizePx = if (size.isSpecified) size.roundToPx() else 0
    // Be at least as big as the minimum dimension in both dimensions
    val width = if (enforcement) {
      maxOf(placeable.width, sizePx)
    } else {
      placeable.width
    }
    val height = if (enforcement) {
      maxOf(placeable.height, sizePx)
    } else {
      placeable.height
    }

    return layout(width, height) {
      val centerX = ((width - placeable.width) / 2f).roundToInt()
      val centerY = ((height - placeable.height) / 2f).roundToInt()
      placeable.place(centerX, centerY)
    }
  }
}

/**
 * CompositionLocal that configures the minimum touch target size for Material components
 * (such as [Button]) to ensure they are accessible. If a component has a visual size
 * that is lower than the minimum touch target size, extra space outside the component will be
 * included. If set to [Dp.Unspecified] there will be no extra space, and so it is possible that if the
 * component is placed near the edge of a layout / near to another component without any padding,
 * there will not be enough space for an accessible touch target.
 */
val LocalMinimumInteractiveComponentSize: ProvidableCompositionLocal<Dp> = staticCompositionLocalOf { 48.dp }
