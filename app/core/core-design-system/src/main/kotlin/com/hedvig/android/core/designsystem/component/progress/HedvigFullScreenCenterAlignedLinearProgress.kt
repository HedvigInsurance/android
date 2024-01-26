package com.hedvig.android.core.designsystem.component.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import kotlinx.coroutines.delay

private const val DEBOUNCE_MILLIS = 200L

@Composable
fun HedvigFullScreenCenterAlignedLinearProgressDebounced(
  title: String,
  modifier: Modifier = Modifier,
  show: Boolean = true,
  debounceMillis: Long = DEBOUNCE_MILLIS,
) {
  var debounce by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(debounceMillis)
    debounce = true
  }

  HedvigFullScreenCenterAlignedLinearProgress(
    title = title,
    modifier = modifier,
    show = show && debounce,
  )
}

@Composable
fun HedvigFullScreenCenterAlignedLinearProgress(title: String, modifier: Modifier = Modifier, show: Boolean = true) {
  Box(modifier) {
    AnimatedVisibility(
      visible = show,
      enter = fadeIn(),
      exit = fadeOut(),
      label = "progress indicator",
      modifier = Modifier.fillMaxSize(),
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(text = title)
          Spacer(Modifier.height(16.dp))
          LinearProgressIndicator(
            strokeCap = StrokeCap.Round,
            modifier = Modifier.defaultMinSize(247.dp),
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigFullScreenCenterAlignedLoadingIndicator() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigFullScreenCenterAlignedLinearProgress(
        title = "Terminating...",
      )
    }
  }
}
