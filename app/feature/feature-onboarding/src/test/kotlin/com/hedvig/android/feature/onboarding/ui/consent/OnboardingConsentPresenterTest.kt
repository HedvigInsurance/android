package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.FakeSettingsDataStore
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
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

internal class OnboardingConsentPresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `allow stores GRANTED and advances to the next step`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val settingsDataStore = FakeSettingsDataStore()
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingConsentPresenter(sessionStore, navigator, settingsDataStore)

    presenter.test(OnboardingConsentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      assertThat(awaitItem()).isInstanceOf<OnboardingConsentUiState.Content>()
      sendEvent(OnboardingConsentEvent.Allow)
      runCurrent()
      assertThat(settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.GRANTED)
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
    }
  }

  @Test
  fun `deny stores DENIED and still advances`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository)
    val settingsDataStore = FakeSettingsDataStore()
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingConsentPresenter(sessionStore, navigator, settingsDataStore)

    presenter.test(OnboardingConsentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingConsentEvent.Deny)
      runCurrent()
      assertThat(settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.DENIED)
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
    }
  }
}
