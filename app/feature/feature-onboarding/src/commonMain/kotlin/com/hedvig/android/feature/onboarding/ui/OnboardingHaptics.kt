package com.hedvig.android.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * [onClick] with the tick every tappable thing in onboarding gives.
 *
 * Wrapping the click rather than exposing the feedback on its own means a control cannot be wired up
 * with the click but without the tick: the flow is meant to feel the same throughout, and the buttons
 * that do not go through [OnboardingStepButtons] have no other reason to remember it.
 *
 * [HapticFeedbackType.LongPress] rather than a lighter type because it is the sharper tick, and the
 * one the flow has used since haptics were added to it.
 */
@Composable
internal fun withOnboardingHaptic(onClick: () -> Unit): () -> Unit {
  val hapticFeedback = LocalHapticFeedback.current
  return {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    onClick()
  }
}
