package com.hedvig.android.feature.onboarding.data

/**
 * Debug affordance: clears the current member's onboarding-seen flag so the flow can be triggered
 * again (on the next resume of an idle Home screen). Exposed publicly so `:app` can wire it to the
 * non-production "Reset onboarding" row in the profile About app screen. Not reachable in
 * production UI.
 */
interface ResetOnboardingSeenUseCase {
  suspend fun invoke()
}
