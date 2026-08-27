package com.hedvig.android.feature.onboarding.ui.welcome

import androidx.compose.runtime.mutableStateListOf
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.navigation.OnboardingKey
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepKey
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.feature.onboarding.testSessionStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class OnboardingWelcomePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `session fetch success shows content with the progress of the whole path`() = runTest {
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(TestBackstack(), sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingWelcomePresenter(sessionStore, navigator)

    presenter.test(OnboardingWelcomeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      val content = awaitItem()
      assertThat(content).isInstanceOf<OnboardingWelcomeUiState.Content>()
      // default test data: consent+phone+theme+coinsured+invite+payment+bundle = 7 steps + welcome
      assertThat((content as OnboardingWelcomeUiState.Content).progress.totalSteps).isEqualTo(8)
      assertThat(content.progress.currentIndex).isEqualTo(0)
    }
  }

  @Test
  fun `disabling analytics costs the progress bar one segment`() = runTest {
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider(), analyticsDisabled = true)
    val navigator = OnboardingNavigator(TestBackstack(), sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingWelcomePresenter(sessionStore, navigator)

    presenter.test(OnboardingWelcomeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      val content = awaitItem()
      assertThat(content).isInstanceOf<OnboardingWelcomeUiState.Content>()
      assertThat((content as OnboardingWelcomeUiState.Content).progress.totalSteps).isEqualTo(7)
    }
  }

  @Test
  fun `session fetch failure shows error, retry refetches`() = runTest {
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(TestBackstack(), sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingWelcomePresenter(sessionStore, navigator)

    presenter.test(OnboardingWelcomeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(ErrorMessage("boom").left())
      assertThat(awaitItem()).isInstanceOf<OnboardingWelcomeUiState.Error>()
      sendEvent(OnboardingWelcomeEvent.Retry)
      skipItems(1) // back to Loading
      repository.onboardingDataResponses.add(testOnboardingData().right())
      assertThat(awaitItem()).isInstanceOf<OnboardingWelcomeUiState.Content>()
    }
  }

  @Test
  fun `get started pushes the first path step onto the backstack`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingKey) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingWelcomePresenter(sessionStore, navigator)

    presenter.test(OnboardingWelcomeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingWelcomeEvent.GetStarted)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))
    }
  }
}
