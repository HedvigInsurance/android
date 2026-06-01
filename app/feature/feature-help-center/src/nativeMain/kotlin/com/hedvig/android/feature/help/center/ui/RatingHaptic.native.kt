package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

/**
 * `Confirm` and `LongPress` are delegated to Compose's iOS impl (they map to
 * `UINotificationFeedbackGenerator.success` and a medium `UIImpactFeedbackGenerator` respectively).
 *
 * The "subtle" 1-3 case and the extra thump on 5 use UIKit directly because Compose's iOS impl
 * routes every other "light" `HapticFeedbackType` to `UISelectionFeedbackGenerator`
 * (imperceptible) and never exposes `UIImpactFeedbackStyleLight`/`Heavy`.
 */
@Composable
actual fun rememberPerformRatingHaptic(hapticFeedback: HapticFeedback): (rating: Int) -> Unit {
  return remember(hapticFeedback) {
    val lightImpact = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    val heavyImpact = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy);
    { rating: Int ->
      when (rating) {
        4 -> {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        }

        5 -> {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
          heavyImpact.impactOccurred()
        }

        else -> {
          lightImpact.impactOccurred()
        }
      }
    }
  }
}
