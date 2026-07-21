package com.hedvig.android.feature.onboarding.ui.invite

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingReferralInformation
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepKey
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class OnboardingInvitePresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `content carries the referral code and incentive`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.InviteFriend)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingInvitePresenter(sessionStore, navigator)

    presenter.test(OnboardingInviteUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingInviteUiState.Content>()
      val content = state as OnboardingInviteUiState.Content
      assertThat(content.code).isEqualTo("CODE")
      assertThat(content.incentiveDisplay).isEqualTo("10 SEK")
    }
  }

  @Test
  fun `continue advances to connect payment`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.InviteFriend)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingInvitePresenter(sessionStore, navigator)

    presenter.test(OnboardingInviteUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      assertThat(awaitItem()).isInstanceOf<OnboardingInviteUiState.Content>()
      sendEvent(OnboardingInviteEvent.Continue)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.ConnectPayment))
    }
  }

  @Test
  fun `step renders error state if the session has no referral information`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.InviteFriend)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingInvitePresenter(sessionStore, navigator)

    presenter.test(OnboardingInviteUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData(referralInformation = null).right())
      assertThat(awaitItem()).isEqualTo(OnboardingInviteUiState.Error)
    }
  }
}
