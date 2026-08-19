package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.FakeSettingsDataStore
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepKey
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
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

  private class TestSetup(storedConsent: AnalyticsConsent = AnalyticsConsent.NOT_DECIDED) {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent)) }
    val repository = FakeOnboardingRepository()
    val settingsDataStore = FakeSettingsDataStore().apply { consent.value = storedConsent }
    private val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val presenter = OnboardingConsentPresenter(
      sessionStore,
      OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase()),
      settingsDataStore,
    )
  }

  /** Lets the initial load complete and returns the first [OnboardingConsentUiState.Content]. */
  private suspend fun MoleculePresenterTestContext<OnboardingConsentEvent, OnboardingConsentUiState>.awaitContent(
    repository: FakeOnboardingRepository,
  ): OnboardingConsentUiState.Content {
    skipItems(1)
    repository.onboardingDataResponses.add(testOnboardingData().right())
    val state = awaitItem()
    assertThat(state).isInstanceOf<OnboardingConsentUiState.Content>()
    return state as OnboardingConsentUiState.Content
  }

  @Test
  fun `the checkmark starts hidden when consent has not been granted before`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      assertThat(awaitContent(setup.repository).checkmarkVisible).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the checkmark starts shown when consent was granted before`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.GRANTED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      assertThat(awaitContent(setup.repository).checkmarkVisible).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `allow stores GRANTED and advances once the checkmark has animated in`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Allow)
      runCurrent()

      assertThat(setup.settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.GRANTED)
      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))

      sendEvent(OnboardingConsentEvent.CheckmarkSettled(checkmarkVisible = true))
      runCurrent()

      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a decision in flight shows the checkmark and disables the buttons`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Allow)
      runCurrent()

      var state = awaitItem() as OnboardingConsentUiState.Content
      while (!state.checkmarkVisible) {
        state = awaitItem() as OnboardingConsentUiState.Content
      }
      assertThat(state.buttonsEnabled).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `deny stores DENIED and advances without waiting when the checkmark was already hidden`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Deny)
      runCurrent()

      assertThat(setup.settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.DENIED)
      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `deny advances once the checkmark has animated out when consent was granted before`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.GRANTED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Deny)
      runCurrent()

      assertThat(setup.settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.DENIED)
      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))

      sendEvent(OnboardingConsentEvent.CheckmarkSettled(checkmarkVisible = false))
      runCurrent()

      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `repeated decisions navigate exactly once`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Allow)
      sendEvent(OnboardingConsentEvent.Allow)
      sendEvent(OnboardingConsentEvent.Deny)
      sendEvent(OnboardingConsentEvent.Close)
      runCurrent()

      assertThat(setup.settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.GRANTED)

      sendEvent(OnboardingConsentEvent.CheckmarkSettled(checkmarkVisible = true))
      runCurrent()
      sendEvent(OnboardingConsentEvent.Allow)
      runCurrent()

      assertThat(setup.backstack.entries).hasSize(2)
      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }
}
