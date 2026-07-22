package com.hedvig.android.feature.onboarding.gate

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.onboarding.data.OnboardingSeenStore
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

/**
 * Decides whether onboarding should be shown for the current member: not killed by the
 * [Feature.DISABLE_ONBOARDING] kill switch, never seen before, and the eager fetch succeeded
 * (caching the session so the flow renders without further loading). On fetch failure this
 * returns false WITHOUT marking seen, so the next app start retries.
 */
interface OnboardingGate {
  suspend fun shouldShowOnboarding(): Boolean
}

@ContributesBinding(ActivityRetainedScope::class)
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingGateImpl(
  private val memberIdService: MemberIdService,
  private val onboardingSeenStore: OnboardingSeenStore,
  private val sessionStore: OnboardingSessionStore,
  private val featureManager: FeatureManager,
) : OnboardingGate {
  override suspend fun shouldShowOnboarding(): Boolean {
    if (featureManager.isFeatureEnabled(Feature.DISABLE_ONBOARDING).first()) {
      logcat(LogPriority.INFO) { "Onboarding is disabled by the kill switch, not showing onboarding" }
      return false
    }
    val memberId = memberIdService.getMemberId().first() ?: return false
    if (onboardingSeenStore.hasSeenOnboarding(memberId)) return false
    return sessionStore.getOrFetchSession().fold(
      ifLeft = { errorMessage ->
        logcat(LogPriority.INFO) { "Onboarding data fetch failed, not showing onboarding: $errorMessage" }
        false
      },
      ifRight = { session -> session.path.isNotEmpty() },
    )
  }
}
