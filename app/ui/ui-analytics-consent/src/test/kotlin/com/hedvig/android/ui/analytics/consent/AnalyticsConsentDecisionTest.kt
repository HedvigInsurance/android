package com.hedvig.android.ui.analytics.consent

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.test.test
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class AnalyticsConsentDecisionTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class FakeSettingsDataStore(initialConsent: AnalyticsConsent) : SettingsDataStore {
    val consent = MutableStateFlow(initialConsent)

    override suspend fun setAnalyticsConsent(consent: AnalyticsConsent) {
      this.consent.value = consent
    }

    override fun observeAnalyticsConsent(): Flow<AnalyticsConsent> = consent

    override suspend fun setTheme(theme: Theme) = error("unused")

    override fun observeTheme(): Flow<Theme?> = error("unused")

    override suspend fun setEmailSubscriptionPreference(subscribe: Boolean) = error("unused")

    override fun observeEmailSubscriptionPreference(): Flow<Boolean> = error("unused")
  }

  private sealed interface TestEvent {
    data class Decide(val consent: AnalyticsConsent) : TestEvent

    data class BadgeSettled(val badge: ConsentBadge?) : TestEvent
  }

  private data class TestState(
    val consent: AnalyticsConsent? = null,
    val badge: ConsentBadge? = null,
    val isDeciding: Boolean = false,
  )

  private class TestPresenter(
    private val settingsDataStore: SettingsDataStore,
  ) : MoleculePresenter<TestEvent, TestState> {
    var decidedCount = 0

    @Composable
    override fun MoleculePresenterScope<TestEvent>.present(lastState: TestState): TestState {
      val decision = rememberAnalyticsConsentDecision(
        settingsDataStore = settingsDataStore,
        lastConsent = lastState.consent,
        onDecided = { decidedCount++ },
      )
      CollectEvents { event ->
        when (event) {
          is TestEvent.Decide -> decision.decide(event.consent)
          is TestEvent.BadgeSettled -> decision.onBadgeSettled(event.badge)
        }
      }
      return TestState(decision.consent, decision.badge, decision.isDeciding)
    }
  }

  @Test
  fun `the badge follows the stored consent`() = runTest {
    val settingsDataStore = FakeSettingsDataStore(AnalyticsConsent.GRANTED)

    TestPresenter(settingsDataStore).test(TestState()) {
      runCurrent()
      assertThat(expectMostRecentItem().badge).isEqualTo(ConsentBadge.Accepted)

      settingsDataStore.consent.value = AnalyticsConsent.DENIED
      runCurrent()
      assertThat(expectMostRecentItem().badge).isEqualTo(ConsentBadge.Denied)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a new answer is stored and acted on once the card has shown it`() = runTest {
    val settingsDataStore = FakeSettingsDataStore(AnalyticsConsent.NOT_DECIDED)
    val presenter = TestPresenter(settingsDataStore)

    presenter.test(TestState()) {
      runCurrent()
      sendEvent(TestEvent.Decide(AnalyticsConsent.GRANTED))
      runCurrent()

      assertThat(settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.GRANTED)
      assertThat(presenter.decidedCount).isEqualTo(0)
      assertThat(expectMostRecentItem().isDeciding).isTrue()

      sendEvent(TestEvent.BadgeSettled(ConsentBadge.Accepted))
      runCurrent()

      assertThat(presenter.decidedCount).isEqualTo(1)
      assertThat(expectMostRecentItem().isDeciding).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `re-confirming the stored answer moves on without waiting for the card`() = runTest {
    val settingsDataStore = FakeSettingsDataStore(AnalyticsConsent.GRANTED)
    val presenter = TestPresenter(settingsDataStore)

    // The card reported nothing to this presenter, as after a restart while the screen stayed shown.
    presenter.test(TestState(consent = AnalyticsConsent.GRANTED)) {
      runCurrent()
      sendEvent(TestEvent.Decide(AnalyticsConsent.GRANTED))
      runCurrent()

      assertThat(presenter.decidedCount).isEqualTo(1)
      assertThat(expectMostRecentItem().isDeciding).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `further answers are ignored while one is being applied`() = runTest {
    val settingsDataStore = FakeSettingsDataStore(AnalyticsConsent.NOT_DECIDED)
    val presenter = TestPresenter(settingsDataStore)

    presenter.test(TestState()) {
      runCurrent()
      sendEvent(TestEvent.Decide(AnalyticsConsent.GRANTED))
      sendEvent(TestEvent.Decide(AnalyticsConsent.DENIED))
      runCurrent()
      sendEvent(TestEvent.BadgeSettled(ConsentBadge.Accepted))
      runCurrent()

      assertThat(settingsDataStore.consent.value).isEqualTo(AnalyticsConsent.GRANTED)
      assertThat(presenter.decidedCount).isEqualTo(1)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a settle report for a different badge does not complete the answer`() = runTest {
    val settingsDataStore = FakeSettingsDataStore(AnalyticsConsent.GRANTED)
    val presenter = TestPresenter(settingsDataStore)

    presenter.test(TestState()) {
      runCurrent()
      sendEvent(TestEvent.BadgeSettled(ConsentBadge.Accepted))
      sendEvent(TestEvent.Decide(AnalyticsConsent.DENIED))
      runCurrent()

      assertThat(presenter.decidedCount).isEqualTo(0)

      sendEvent(TestEvent.BadgeSettled(ConsentBadge.Denied))
      runCurrent()

      assertThat(presenter.decidedCount).isEqualTo(1)
      cancelAndIgnoreRemainingEvents()
    }
  }
}
