package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.FakeSettingsDataStore
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepKey
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.feature.onboarding.testSessionStore
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
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
    private val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
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
  fun `no badge shows when consent has not been decided before`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      assertThat(awaitContent(setup.repository).badge).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the checkmark badge starts shown when consent was granted before`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.GRANTED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      assertThat(awaitContent(setup.repository).badge).isEqualTo(ConsentBadge.Accepted)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the cross badge starts shown when consent was denied before`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.DENIED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      assertThat(awaitContent(setup.repository).badge).isEqualTo(ConsentBadge.Denied)
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

      sendEvent(OnboardingConsentEvent.BadgeSettled(ConsentBadge.Accepted))
      runCurrent()

      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `deny stores DENIED and advances once the cross has animated in`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Deny)
      runCurrent()

      assertThat(setup.settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.DENIED)
      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))

      sendEvent(OnboardingConsentEvent.BadgeSettled(ConsentBadge.Denied))
      runCurrent()

      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a decision in flight shows its badge and disables the buttons`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Deny)
      runCurrent()

      var state = awaitItem() as OnboardingConsentUiState.Content
      while (state.badge == null) {
        state = awaitItem() as OnboardingConsentUiState.Content
      }
      assertThat(state.badge).isEqualTo(ConsentBadge.Denied)
      assertThat(state.buttonsEnabled).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `deny advances once the badge has swapped when consent was granted before`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.GRANTED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Deny)
      runCurrent()

      assertThat(setup.settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.DENIED)
      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))

      sendEvent(OnboardingConsentEvent.BadgeSettled(ConsentBadge.Denied))
      runCurrent()

      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the buttons are usable again once the decision has navigated forward`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.NOT_DECIDED)

    setup.presenter.test(OnboardingConsentUiState.Loading) {
      awaitContent(setup.repository)
      sendEvent(OnboardingConsentEvent.Allow)
      runCurrent()
      sendEvent(OnboardingConsentEvent.BadgeSettled(ConsentBadge.Accepted))
      runCurrent()

      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      val state = expectMostRecentItem() as OnboardingConsentUiState.Content
      assertThat(state.buttonsEnabled).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a presenter restarted on this screen re-derives the badge from stored consent`() = runTest {
    val setup = TestSetup(storedConsent = AnalyticsConsent.DENIED)
    val restoredState = OnboardingConsentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 1),
      badge = null,
      buttonsEnabled = true,
    )

    setup.presenter.test(restoredState) {
      runCurrent()

      val state = expectMostRecentItem() as OnboardingConsentUiState.Content
      assertThat(state.badge).isEqualTo(ConsentBadge.Denied)
      assertThat(state.buttonsEnabled).isTrue()
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

      sendEvent(OnboardingConsentEvent.BadgeSettled(ConsentBadge.Accepted))
      runCurrent()

      assertThat(setup.backstack.entries).hasSize(2)
      assertThat(setup.backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
      cancelAndIgnoreRemainingEvents()
    }
  }
}
