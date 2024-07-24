package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.createRippleModifierNode
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.unit.Dp
import com.hedvig.android.design.system.hedvig.tokens.StateTokens

/**
 * Source:
 * https://cs.android.com/androidx/platform/frameworks/support/+/710392834c2317072387b04235d2ad02012abaf8:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Ripple.kt;l=252
 */
@Stable
fun ripple(
  bounded: Boolean = true,
  radius: Dp = Dp.Unspecified,
  color: Color = Color.Unspecified,
): IndicationNodeFactory {
  return if (radius == Dp.Unspecified && color == Color.Unspecified) {
    if (bounded) return DefaultBoundedRipple else DefaultUnboundedRipple
  } else {
    RippleNodeFactory(bounded, radius, color)
  }
}

@Suppress("unused")
@Stable
internal fun ripple(color: ColorProducer, bounded: Boolean = true, radius: Dp = Dp.Unspecified): IndicationNodeFactory {
  return RippleNodeFactory(bounded, radius, color)
}

internal object RippleDefaults {
  val RippleAlpha: RippleAlpha = RippleAlpha(
    pressedAlpha = StateTokens.PressedStateLayerOpacity,
    focusedAlpha = StateTokens.FocusStateLayerOpacity,
    draggedAlpha = StateTokens.DraggedStateLayerOpacity,
    hoveredAlpha = StateTokens.HoverStateLayerOpacity,
  )
}

internal val LocalRippleConfiguration: ProvidableCompositionLocal<RippleConfiguration> = compositionLocalOf {
  RippleConfiguration()
}

@Immutable
internal data class RippleConfiguration(
  val isEnabled: Boolean = true,
  val color: Color = Color.Unspecified,
  val rippleAlpha: RippleAlpha? = null,
)

@Stable
private class RippleNodeFactory private constructor(
  private val bounded: Boolean,
  private val radius: Dp,
  private val colorProducer: ColorProducer?,
  private val color: Color,
) : IndicationNodeFactory {
  constructor(
    bounded: Boolean,
    radius: Dp,
    colorProducer: ColorProducer,
  ) : this(bounded, radius, colorProducer, Color.Unspecified)

  constructor(
    bounded: Boolean,
    radius: Dp,
    color: Color,
  ) : this(bounded, radius, null, color)

  override fun create(interactionSource: InteractionSource): DelegatableNode {
    val colorProducer = colorProducer ?: ColorProducer { color }
    return DelegatingThemeAwareRippleNode(interactionSource, bounded, radius, colorProducer)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is RippleNodeFactory) return false

    if (bounded != other.bounded) return false
    if (radius != other.radius) return false
    if (colorProducer != other.colorProducer) return false
    return color == other.color
  }

  override fun hashCode(): Int {
    var result = bounded.hashCode()
    result = 31 * result + radius.hashCode()
    result = 31 * result + colorProducer.hashCode()
    result = 31 * result + color.hashCode()
    return result
  }
}

private class DelegatingThemeAwareRippleNode(
  private val interactionSource: InteractionSource,
  private val bounded: Boolean,
  private val radius: Dp,
  private val color: ColorProducer,
) : DelegatingNode(),
  CompositionLocalConsumerModifierNode,
  ObserverModifierNode {
  private var rippleNode: DelegatableNode? = null

  override fun onAttach() {
    updateConfiguration()
  }

  override fun onObservedReadsChanged() {
    updateConfiguration()
  }

  /**
   * Handles changes to [RippleConfiguration.isEnabled]. Changes to [RippleConfiguration.color] and
   * [RippleConfiguration.rippleAlpha] are handled as part of the ripple definition.
   */
  private fun updateConfiguration() {
    observeReads {
      val configuration = currentValueOf(LocalRippleConfiguration)
      if (!configuration.isEnabled) {
        removeRipple()
      } else {
        if (rippleNode == null) attachNewRipple()
      }
    }
  }

  private fun attachNewRipple() {
    val calculateColor = ColorProducer {
      val userDefinedColor = color()
      if (userDefinedColor.isSpecified) {
        userDefinedColor
      } else {
        val rippleConfiguration = currentValueOf(LocalRippleConfiguration)
        if (rippleConfiguration.color.isSpecified) {
          rippleConfiguration.color
        } else {
          currentValueOf(LocalContentColor)
        }
      }
    }

    val calculateRippleAlpha = {
      val rippleConfiguration = currentValueOf(LocalRippleConfiguration)
      rippleConfiguration.rippleAlpha ?: RippleDefaults.RippleAlpha
    }

    rippleNode = delegate(
      createRippleModifierNode(
        interactionSource,
        bounded,
        radius,
        calculateColor,
        calculateRippleAlpha,
      ),
    )
  }

  private fun removeRipple() {
    rippleNode?.let { undelegate(it) }
  }
}

private val DefaultBoundedRipple = RippleNodeFactory(
  bounded = true,
  radius = Dp.Unspecified,
  color = Color.Unspecified,
)
private val DefaultUnboundedRipple = RippleNodeFactory(
  bounded = false,
  radius = Dp.Unspecified,
  color = Color.Unspecified,
)
