package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
actual fun rememberPerformRatingHaptic(hapticFeedback: HapticFeedback): (rating: Int) -> Unit {
  return remember(hapticFeedback) {
    { rating ->
      val type = when (rating) {
        4, 5 -> HapticFeedbackType.Confirm
        else -> HapticFeedbackType.TextHandleMove
      }
      hapticFeedback.performHapticFeedback(type)
    }
  }
}
