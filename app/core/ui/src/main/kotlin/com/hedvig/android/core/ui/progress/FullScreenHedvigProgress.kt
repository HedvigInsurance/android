package com.hedvig.android.core.ui.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun FullScreenHedvigProgress(
  modifier: Modifier = Modifier,
  show: Boolean = true,
) {
  AnimatedVisibility(
    visible = show,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier.fillMaxSize(),
  ) {
    Box(contentAlignment = Alignment.Center) {
      RotatingHedvigButton()
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
