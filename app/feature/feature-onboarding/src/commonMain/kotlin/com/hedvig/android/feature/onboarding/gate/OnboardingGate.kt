package com.hedvig.android.feature.onboarding.gate

/**
 * Decides whether onboarding should be shown for the current member: not killed by the
 * `Feature.DISABLE_ONBOARDING` kill switch, never seen before, and the eager fetch succeeded
 * (caching the session so the flow renders without further loading). On fetch failure this
 * returns false WITHOUT marking seen, so the next app start retries.
 */
interface OnboardingGate {
  suspend fun shouldShowOnboarding(): Boolean

  /**
   * Suspends while the onboarding flow is on the back stack and marks it seen once it leaves, no
   * matter how it left: completion, the close button (both already mark seen, this is idempotent),
   * or a plain system back on the welcome root, which would otherwise let the flow silently
   * reappear on the next resume. Observing removal instead of intercepting back keeps predictive
   * back fully native. Returns immediately if the flow is not currently showing.
   */
  suspend fun markSeenWhenOnboardingDismissed()
}
