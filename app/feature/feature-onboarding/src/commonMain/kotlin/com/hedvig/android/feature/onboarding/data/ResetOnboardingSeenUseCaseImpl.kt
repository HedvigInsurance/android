package com.hedvig.android.feature.onboarding.data

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

/**
 * Debug affordance: clears the current member's onboarding-seen flag so the flow can be triggered
 * again (on the next resume of an idle Home screen). Exposed publicly so `:app` can wire it to the
 * non-production "Reset onboarding" row in the profile About app screen. Not reachable in
 * production UI.
 */
interface ResetOnboardingSeenUseCase {
  suspend fun invoke()
}

@ContributesBinding(AppScope::class)
@Inject
internal class ResetOnboardingSeenUseCaseImpl(
  private val memberIdService: MemberIdService,
  private val onboardingSeenStore: OnboardingSeenStore,
) : ResetOnboardingSeenUseCase {
  override suspend fun invoke() {
    val memberId = memberIdService.getMemberId().first()
    if (memberId == null) {
      logcat { "Reset onboarding requested without a member id; nothing to reset" }
      return
    }
    onboardingSeenStore.resetOnboardingSeen(memberId)
    logcat { "Onboarding seen flag reset for the current member" }
  }
}
