package com.hedvig.android.feature.onboarding.data

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

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
