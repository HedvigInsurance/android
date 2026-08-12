package com.hedvig.android.feature.onboarding.ui.bundle

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingKey
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

internal class OnboardingBundlePresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  private class FakeCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    var invoked: Boolean = false

    override suspend fun invoke() {
      invoked = true
    }
  }

  @Test
  fun `content lists the cross sells`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingBundlePresenter(sessionStore, navigator)

    presenter.test(OnboardingBundleUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingBundleUiState.Content>()
      val content = state as OnboardingBundleUiState.Content
      assertThat(content.crossSells.single().title).isEqualTo("Pet")
    }
  }

  @Test
  fun `continue to app completes onboarding`() = runTest {
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    val backstack = TestBackstack().apply {
      entries.add(NonOnboardingKey)
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, completeOnboarding)
    val presenter = OnboardingBundlePresenter(sessionStore, navigator)

    presenter.test(OnboardingBundleUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      assertThat(awaitItem()).isInstanceOf<OnboardingBundleUiState.Content>()
      sendEvent(OnboardingBundleEvent.ContinueToApp)
      runCurrent()
      assertThat(completeOnboarding.invoked).isTrue()
      assertThat(backstack.entries).containsExactly(NonOnboardingKey)
    }
  }
}

private data object NonOnboardingKey : HedvigNavKey
