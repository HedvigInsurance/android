package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens

@Composable
fun ThreeDotsLoading(
  modifier: Modifier = Modifier,
  stableColor: Color = ColorSchemeKeyTokens.FillPrimary.value,
  temporaryColor: Color = ColorSchemeKeyTokens.SurfaceSecondaryTransparent.value,
  stableScale: Float = 1f,
  temporaryScale: Float = 0.9f,
  circleRadius: Dp = 6.dp,
) {
  val transition = rememberInfiniteTransition()
  Row(
    horizontalArrangement = Arrangement.spacedBy(circleRadius),
    modifier = modifier,
  ) {
    for (index in (0..<numberOfDots)) {
      // Each dot breathes continuously between its stable and temporary state. RepeatMode.Reverse
      // (rather than Restart) bounces smoothly with no velocity reset each loop, and a stagger shorter
      // than the breath overlaps the dots into a soft wave rather than a sequential blink. Delaying
      // each dot by its index makes that wave travel left to right.
      val fraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(dotBreathDurationMilliseconds, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
          initialStartOffset = StartOffset(index * dotStaggerMilliseconds),
        ),
      )
      Box(
        Modifier
          .size(circleRadius)
          .graphicsLayer {
            val scale = stableScale + (temporaryScale - stableScale) * fraction
            scaleX = scale
            scaleY = scale
          }
          .background(lerp(stableColor, temporaryColor, fraction), CircleShape),
      )
    }
  }
}

// One dot's travel from stable to temporary; RepeatMode.Reverse doubles this into a full breath.
private const val dotBreathDurationMilliseconds = 600

// Shorter than the breath so consecutive dots overlap into a wave instead of blinking one at a time.
private const val dotStaggerMilliseconds = 200
private const val numberOfDots = 3

// @HedvigPreview
@Preview
@Composable
private fun PreviewThreeDotsLoading() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Box(Modifier.padding(6.dp)) {
        ThreeDotsLoading()
      }
    }
  }
}
