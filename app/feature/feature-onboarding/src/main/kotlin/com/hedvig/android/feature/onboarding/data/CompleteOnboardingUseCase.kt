package com.hedvig.android.feature.onboarding.data

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

internal interface CompleteOnboardingUseCase {
  suspend fun invoke()
}

@ContributesBinding(AppScope::class)
@Inject
internal class CompleteOnboardingUseCaseImpl(
  private val memberIdService: MemberIdService,
  private val onboardingSeenStore: OnboardingSeenStore,
) : CompleteOnboardingUseCase {
  override suspend fun invoke() {
    val memberId = memberIdService.getMemberId().first()
    if (memberId == null) {
      logcat(LogPriority.WARN) { "Completing onboarding without a member id; seen flag not stored" }
      return
    }
    onboardingSeenStore.markOnboardingSeen(memberId)
  }
}
