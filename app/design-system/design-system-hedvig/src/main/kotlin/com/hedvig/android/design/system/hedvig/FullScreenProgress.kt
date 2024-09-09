package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

private const val DEBOUNCE_MILLIS = 200L

@Composable
fun HedvigFullScreenCenterAlignedProgressDebounced(
  modifier: Modifier = Modifier,
  show: Boolean = true,
  debounceMillis: Long = DEBOUNCE_MILLIS,
) {
  var debounce by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(debounceMillis)
    debounce = true
  }

  HedvigFullScreenCenterAlignedProgress(
    modifier = modifier,
    show = show && debounce,
  )
}

@Composable
fun HedvigFullScreenCenterAlignedProgress(modifier: Modifier = Modifier, show: Boolean = true) {
  Box(modifier) {
    AnimatedVisibility(
      visible = show,
      enter = fadeIn(),
      exit = fadeOut(),
      label = "three dots loading animated visibility",
      modifier = Modifier.fillMaxSize(),
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        ThreeDotsLoading()
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigFullScreenCenterAlignedProgress() {
  HedvigTheme {
    HedvigFullScreenCenterAlignedProgress()
  }
}
