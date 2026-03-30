package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Composable
internal fun BlinkingAiDot(
  modifier: Modifier = Modifier,
  isAnimating: Boolean = true,
  durationMillis: Int = 800,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "blink")

  val alpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "alpha",
  )
  val color = HedvigTheme.colorScheme.signalGreenElement
  Spacer(
    modifier
      .wrapContentSize(Alignment.CenterStart)
      .size(20.dp)
      .padding(1.dp)
      .alpha(if (isAnimating) alpha else 1f)
      .background(color, CircleShape),
  )
}
