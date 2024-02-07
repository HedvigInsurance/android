package com.hedvig.android.core.designsystem.component.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
  val debounce by produceState(false) {
    delay(debounceMillis)
    value = true
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
            modifier = Modifier
              .fillMaxWidth(2 / 3f)
              .requiredWidthIn(min = 64.dp)
              .widthIn(max = 240.dp)
          )
        }
      }
    }
  }
}

@Preview(device = "spec:width=1100px,height=300px,dpi=440")
@Preview(device = "spec:width=700px,height=300px,dpi=440")
@Preview(device = "spec:width=500px,height=300px,dpi=440")
@Preview(device = "spec:width=350px,height=300px,dpi=440")
@Preview(device = "spec:width=200px,height=300px,dpi=440")
@Preview(device = "spec:width=100px,height=300px,dpi=440")
@Composable
private fun PreviewHedvigFullScreenCenterAlignedLoadingIndicator() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box {
        HedvigFullScreenCenterAlignedLinearProgress(title = "Terminating...")
        Box(
          Modifier
            .height(2.dp)
            .width(240.dp)
            .offset(y = 12.dp)
            .background(Color.Red)
            .align(Alignment.Center),
        )
        Box(
          Modifier
            .height(2.dp)
            .width(64.dp)
            .offset(y = 6.dp)
            .background(Color.Red)
            .align(Alignment.Center),
        )
      }
    }
  }
}
