package com.hedvig.android.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun FullScreenHedvigProgress(
  show: Boolean,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = show,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier.fillMaxSize(),
  ) {
    Box(
      contentAlignment = Alignment.Center,
    ) {
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
  }
}

@HedvigPreview
@Composable
private fun PreviewFullScreenTransparentProgressOverlay() {
  HedvigTheme {
    FullScreenProgressOverlay(true)
  }
}
