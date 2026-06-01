package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback

/**
 * Returns a function that plays a haptic for the given article rating (1..5).
 *
 * Compose Multiplatform's iOS [androidx.compose.ui.hapticfeedback.HapticFeedback] maps most "subtle"
 * [androidx.compose.ui.hapticfeedback.HapticFeedbackType] values to UISelectionFeedbackGenerator,
 * which is imperceptible. This abstraction bypasses that on iOS by calling UIKit generators directly.
 */
@Composable
expect fun rememberPerformRatingHaptic(hapticFeedback: HapticFeedback): (rating: Int) -> Unit
