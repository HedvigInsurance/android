package com.hedvig.android.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalInspectionMode
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

/**
 * todo: Add lint check to not call this from composables not annotated with @Preview, @HedvigPreview or similar
 */
@Composable
fun HedvigPreviewLayout(content: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit) {
  if (LocalInspectionMode.current) {
    HedvigTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
        SharedTransitionLayout {
          AnimatedContent(Unit) { ignored ->
            @Suppress("UNUSED_EXPRESSION")
            ignored // Use the target state so that lint stops complaining
            this@SharedTransitionLayout.content(this)
          }
        }
      }
    }
  } else {
    LaunchedEffect(Unit) {
      logcat(LogPriority.ASSERT) { "HedvigPreviewLayout is a no-op in production code" }
    }
  }
}
