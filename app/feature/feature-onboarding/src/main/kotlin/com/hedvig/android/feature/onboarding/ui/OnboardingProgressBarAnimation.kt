package com.hedvig.android.feature.onboarding.ui

import androidx.compose.runtime.mutableStateMapOf
import com.hedvig.android.core.common.di.ActivityRetainedScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Single source of truth for the onboarding progress bar's fill, shared by every step so the fill
 * moves as one continuous value across the header's shared-element transition.
 *
 * Each step is a separate composition. While a transition runs, both the outgoing and incoming steps
 * contribute their own target fill together with their transition presence (1 when fully on screen,
 * 0 when gone). [displayedFraction] blends them by presence, so:
 * - both bars render the identical value every frame, hiding the `sharedBounds` crossfade,
 * - the fill follows the transition, including a predictive-back gesture and its seek,
 * - a cancelled gesture returns smoothly as the presences return, with nothing left stranded,
 * - a single step on screen simply shows its own fill, so fresh launches and process-death restores
 *   land on the right fill with no sweep.
 *
 * Scoped to the onboarding flow's lifetime, like
 * [com.hedvig.android.feature.onboarding.data.OnboardingSessionStore].
 */
@Inject
@SingleIn(ActivityRetainedScope::class)
internal class OnboardingProgressBarAnimation {
  private val contributions = mutableStateMapOf<Any, Contribution>()

  val displayedFraction: Float
    get() {
      var weighted = 0f
      var totalPresence = 0f
      for (contribution in contributions.values) {
        weighted += contribution.fraction * contribution.presence
        totalPresence += contribution.presence
      }
      if (totalPresence > PresenceEpsilon) return weighted / totalPresence
      // No meaningful presence yet (e.g. the very first frame): fall back to the plain average so
      // the bar never flashes empty.
      if (contributions.isEmpty()) return 0f
      return contributions.values.sumOf { it.fraction.toDouble() }.toFloat() / contributions.size
    }

  fun contribute(key: Any, fraction: Float, presence: Float) {
    contributions[key] = Contribution(fraction, presence)
  }

  fun release(key: Any) {
    contributions.remove(key)
  }

  private data class Contribution(val fraction: Float, val presence: Float)

  private companion object {
    const val PresenceEpsilon = 0.0001f
  }
}
