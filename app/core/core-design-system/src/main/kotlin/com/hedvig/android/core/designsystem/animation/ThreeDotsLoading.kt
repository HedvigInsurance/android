package com.hedvig.android.core.designsystem.animation

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun ThreeDotsLoading(
  modifier: Modifier = Modifier,
  stableColor: Color = LocalContentColor.current,
  temporaryColor: Color = LocalContentColor.current.copy(alpha = DisabledAlpha),
  stableScale: Float = 1f,
  temporaryScale: Float = 0.9f,
  circleRadius: Dp = 6.dp,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(circleRadius),
    modifier = modifier,
  ) {
    for (index in (0..numberOfDots - 1)) {
      LoadingDot(
        circleRadius = circleRadius,
        startDelay = index * animationDurationMilliseconds,
        stableColor = stableColor,
        temporaryColor = temporaryColor,
        stableScale = stableScale,
        temporaryScale = temporaryScale,
      )
    }
  }
}

@Composable
private fun LoadingDot(
  circleRadius: Dp,
  startDelay: Int,
  stableColor: Color,
  temporaryColor: Color,
  stableScale: Float,
  temporaryScale: Float,
  modifier: Modifier = Modifier,
) {
  val transition = rememberInfiniteTransition()
  val color by transition.animateColor(
    // ignored since the keyFrames DSL overrides it
    initialValue = stableColor,
    // ignored since the keyFrames DSL overrides it
    targetValue = stableColor,
    animationSpec = dotInfiniteRepeatableSpec(stableColor, temporaryColor, startDelay),
  )
  val scale by transition.animateFloat(
    // ignored since the keyFrames DSL overrides it
    initialValue = 1f,
    // ignored since the keyFrames DSL overrides it
    targetValue = 1f,
    animationSpec = dotInfiniteRepeatableSpec(stableScale, temporaryScale, startDelay),
  )
  Box(
    modifier
      .size(circleRadius)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .background(color, CircleShape),
  )
}

@Stable
@Composable
private fun <T> dotInfiniteRepeatableSpec(
  stableValue: T,
  temporaryValue: T,
  startDelay: Int,
): InfiniteRepeatableSpec<T> = infiniteRepeatable(
  animation = keyframes {
    durationMillis = animationDurationMilliseconds * numberOfDots
    stableValue at 0
    temporaryValue at animationDurationMilliseconds using EaseIn
    stableValue at animationDurationMilliseconds * 2 using EaseOut
    stableValue at durationMillis
  },
  repeatMode = RepeatMode.Restart,
  initialStartOffset = StartOffset(startDelay),
)

// The duration of half of the animation. During this duration, one item does the "out" animation, and at the same
// time, the next item does the "in" animation, they happen in parallel.
private const val animationDurationMilliseconds = 500
private const val numberOfDots = 3

@HedvigPreview
@Composable
private fun PreviewThreeDotsLoading() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.primary) {
      Box(Modifier.padding(6.dp)) {
        ThreeDotsLoading()
      }
    }
  }
}
