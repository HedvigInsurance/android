package com.hedvig.android.feature.onboarding.ui.bundle

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * A single line of text that, when it is wider than the space it is given, scrolls left to reveal
 * its end, pauses, then scrolls back to the start and pauses again, looping. Text that already fits
 * stays still. Scroll duration is proportional to the hidden overflow, so longer text scrolls for
 * longer at a constant reading speed rather than faster.
 */
@Composable
internal fun AutoScrollingText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
  var containerWidth by remember { mutableIntStateOf(0) }
  var contentWidth by remember { mutableIntStateOf(0) }
  val offset = remember { Animatable(0f) }
  val density = LocalDensity.current

  LaunchedEffect(containerWidth, contentWidth) {
    val overflow = (contentWidth - containerWidth).toFloat()
    if (overflow <= 0f) {
      offset.snapTo(0f)
      return@LaunchedEffect
    }
    val durationMillis = with(density) { (overflow / SCROLL_VELOCITY_PER_SECOND.toPx() * 1000f).roundToInt() }
    val scrollSpec = tween<Float>(durationMillis, easing = LinearEasing)
    while (true) {
      delay(PAUSE_MILLIS)
      offset.animateTo(-overflow, scrollSpec)
      delay(PAUSE_MILLIS)
      offset.animateTo(0f, scrollSpec)
    }
  }

  Box(
    modifier = modifier
      .clipToBounds()
      .onSizeChanged { containerWidth = it.width },
  ) {
    HedvigText(
      text = text,
      style = style,
      color = color,
      maxLines = 1,
      softWrap = false,
      modifier = Modifier
        .wrapContentWidth(align = Alignment.Start, unbounded = true)
        .onSizeChanged { contentWidth = it.width }
        .offset { IntOffset(offset.value.roundToInt(), 0) },
    )
  }
}

private val SCROLL_VELOCITY_PER_SECOND = 30.dp
private const val PAUSE_MILLIS = 1500L
