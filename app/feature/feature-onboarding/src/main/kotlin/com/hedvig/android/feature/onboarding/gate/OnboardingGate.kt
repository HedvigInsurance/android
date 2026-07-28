package com.hedvig.android.feature.onboarding.gate

import androidx.compose.runtime.snapshotFlow
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSeenStore
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingKey
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Decides whether onboarding should be shown for the current member: not killed by the
 * [Feature.DISABLE_ONBOARDING] kill switch, never seen before, and the eager fetch succeeded
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

@ContributesBinding(ActivityRetainedScope::class)
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingGateImpl(
  private val memberIdService: MemberIdService,
  private val onboardingSeenStore: OnboardingSeenStore,
  private val sessionStore: OnboardingSessionStore,
  private val featureManager: FeatureManager,
  private val backstack: Backstack,
  private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : OnboardingGate {
  override suspend fun shouldShowOnboarding(): Boolean {
    // Fail closed when no flag value can be obtained: show nothing without marking onboarding seen, so a
    // later launch that does get a value decides properly. awaitReady() never completes while the app
    // has never reached the flag backend, so the timeout is what bounds that case.
    val flagsAvailable = withTimeoutOrNull(FLAG_READY_TIMEOUT) {
      featureManager.awaitReady()
      true
    } ?: false
    if (!flagsAvailable) {
      logcat(LogPriority.INFO) { "No flag value available yet, not showing onboarding this launch" }
      return false
    }
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

  override suspend fun markSeenWhenOnboardingDismissed() {
    if (backstack.entries.none { it is OnboardingKey }) return
    snapshotFlow { backstack.entries.none { it is OnboardingKey } }.first { it }
    // Idempotent: completion and the close button have already marked seen through this same
    // use case; this additionally covers a system back on the welcome root.
    completeOnboardingUseCase.invoke()
  }
}

// Comfortably covers a local backup restore (near-instant) and a first successful Unleash poll (poll
// interval is 2s) plus network latency. When no value can be obtained this whole window is spent before
// failing closed, but it runs behind an already-rendered Home, so it is not user visible.
private val FLAG_READY_TIMEOUT = 5.seconds
