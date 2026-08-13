package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Scrolls the content horizontally when it is wider than the space it is given: left to reveal its
 * end, a pause, then back to the start and another pause, looping. Content that already fits stays
 * still. Scroll duration is proportional to the hidden overflow, so the speed stays constant rather
 * than long content whipping past. Intended for a single, non-wrapping line of text.
 *
 * @param velocity how far the content travels per second while scrolling
 * @param pause how long to hold at each end before scrolling the other way
 */
fun Modifier.autoScrollingMarquee(
  velocity: Dp = DEFAULT_VELOCITY_PER_SECOND,
  pause: Duration = DEFAULT_PAUSE,
): Modifier = clipToBounds() then AutoScrollingMarqueeElement(velocity, pause)

private data class AutoScrollingMarqueeElement(
  val velocity: Dp,
  val pause: Duration,
) : ModifierNodeElement<AutoScrollingMarqueeNode>() {
  override fun create() = AutoScrollingMarqueeNode(velocity, pause)

  override fun update(node: AutoScrollingMarqueeNode) = node.update(velocity, pause)

  override fun InspectorInfo.inspectableProperties() {
    name = "autoScrollingMarquee"
    properties["velocity"] = velocity
    properties["pause"] = pause
  }
}

private class AutoScrollingMarqueeNode(
  private var velocity: Dp,
  private var pause: Duration,
) : Modifier.Node(), LayoutModifierNode {
  private val offset = Animatable(0f)
  private var overflow = 0f
  private var animation: Job? = null

  fun update(velocity: Dp, pause: Duration) {
    this.velocity = velocity
    this.pause = pause
    restart()
  }

  override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
    val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
    val width = placeable.width.coerceAtMost(constraints.maxWidth)
    val newOverflow = (placeable.width - width).toFloat()
    if (newOverflow != overflow) {
      overflow = newOverflow
      restart()
    }
    return layout(width, placeable.height) {
      placeable.place(offset.value.roundToInt(), 0)
    }
  }

  private fun restart() {
    animation?.cancel()
    animation = coroutineScope.launch {
      if (overflow <= 0f) {
        offset.snapTo(0f)
        return@launch
      }
      val durationMillis = (overflow / with(requireDensity()) { velocity.toPx() } * 1000f).roundToInt()
      val scrollSpec = tween<Float>(durationMillis, easing = LinearEasing)
      offset.snapTo(0f)
      while (isActive) {
        delay(pause)
        offset.animateTo(-overflow, scrollSpec)
        delay(pause)
        offset.animateTo(0f, scrollSpec)
      }
    }
  }
}

private val DEFAULT_VELOCITY_PER_SECOND = 30.dp
private val DEFAULT_PAUSE = 1500.milliseconds
