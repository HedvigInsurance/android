package com.hedvig.feature.claim.chat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ThreeDotLoadingIndicator(
  modifier: Modifier = Modifier,
  dotSize: Dp = 8.dp,
  dotColor: Color = Color.Gray,
  animationDelay: Int = 150,
  pulseDuration: Int = 900,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "fadingLoadingTransition")

  @Composable
  fun getDotAlpha(dotIndex: Int): Float {
    val delay = dotIndex * animationDelay
    val dotAlpha = infiniteTransition.animateFloat(
      initialValue = 0.2f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = keyframes {
          durationMillis = pulseDuration
          delayMillis = delay
          0.2f at 0 with LinearOutSlowInEasing
          1f at pulseDuration / 3 with FastOutSlowInEasing
          0.2f at pulseDuration
        },
        repeatMode = RepeatMode.Restart,
      ),
      label = "dotAlpha${dotIndex}",
    )
    return dotAlpha.value
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    FilledCircle(
      diameter = dotSize,
      circleColor = dotColor.copy(alpha = getDotAlpha(0)), // Apply alpha here
    )

    FilledCircle(
      diameter = dotSize,
      circleColor = dotColor.copy(alpha = getDotAlpha(1)), // Apply alpha here
    )

    FilledCircle(
      diameter = dotSize,
      circleColor = dotColor.copy(alpha = getDotAlpha(2)), // Apply alpha here
    )
  }
}

@Composable
fun FilledCircle(diameter: Dp, circleColor: Color) {
  Box(
    modifier = Modifier
      .size(diameter)
      .clip(CircleShape)
      .background(circleColor),
  )
}


