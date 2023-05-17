package com.hedvig.android.core.ui.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun FullScreenProgressOverlay(
  show: Boolean,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = show,
    enter = fadeIn(animationSpec = tween(500)),
    exit = fadeOut(animationSpec = tween(500, delayMillis = 400)),
    modifier = modifier,
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colors.background,
    ) {
      Box(contentAlignment = Alignment.Center) {
        RotatingHedvigButton()
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFullScreenProgressOverlay() {
  HedvigTheme {
    FullScreenProgressOverlay(true)
  }
}
