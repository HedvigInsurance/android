package com.hedvig.android.core.ui.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.ui.R

@Composable
internal fun RotatingHedvigButton() {
  val infiniteTransition = rememberInfiniteTransition()
  val angle by infiniteTransition.animateFloat(
    initialValue = 0F,
    targetValue = 360F,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
    ),
  )

  Icon(
    painter = painterResource(R.drawable.ic_hedvig_h_progress),
    modifier = Modifier
      .graphicsLayer {
        rotationZ = angle
      }
      .size(32.dp),
    contentDescription = null,
  )
}
