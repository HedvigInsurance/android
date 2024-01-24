package com.hedvig.android.core.designsystem.component.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEBOUNCE_MILLIS = 200L

@Composable
fun HedvigFakeProgressIndicatorDebounced(
  title: String,
  modifier: Modifier = Modifier,
  show: Boolean = true,
  debounceMillis: Long = DEBOUNCE_MILLIS,
  onComplete: () -> Unit,
) {
  var debounce by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(debounceMillis)
    debounce = true
  }

  HedvigFakeProgressIndicator(
    title = title,
    modifier = modifier,
    show = show && debounce,
    onComplete = onComplete,
  )
}

@Composable
fun HedvigFakeProgressIndicator(
  title: String,
  modifier: Modifier = Modifier,
  show: Boolean = true,
  onComplete: () -> Unit,
) {
  var loading by remember { mutableStateOf(true) }
  var progress by remember { mutableFloatStateOf(0f) }
  val scope = rememberCoroutineScope()
  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    label = "progress animation",
  )

  LaunchedEffect(Unit) {
    scope.launch {
      loadProgress { updatedProgress ->
        progress = updatedProgress
      }
      loading = false
    }
  }

  if (progress == 1f) {
    onComplete()
  }

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
            progress = animatedProgress,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.defaultMinSize(247.dp),
          )
        }
      }
    }
  }
}

private suspend fun loadProgress(updateProgress: (Float) -> Unit) {
  for (i in 1..100) {
    if (i == 100) {
      delay(1000)
    }
    updateProgress(i.toFloat() / 100)
    delay((200 / i).toLong())
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigFullScreenCenterAlignedLoadingIndicator() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigFakeProgressIndicator(
        title = "Terminating...",
        onComplete = {},
      )
    }
  }
}
