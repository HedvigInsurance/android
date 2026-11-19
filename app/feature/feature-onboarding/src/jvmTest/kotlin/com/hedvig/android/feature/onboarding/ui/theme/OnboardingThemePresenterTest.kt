package com.hedvig.android.feature.onboarding.ui.theme

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.FakeSettingsDataStore
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepKey
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.feature.onboarding.testSessionStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class OnboardingThemePresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `selected theme defaults to SYSTEM_DEFAULT when nothing stored`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.Theme)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val settingsDataStore = FakeSettingsDataStore()
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingThemePresenter(sessionStore, navigator, settingsDataStore)

    presenter.test(OnboardingThemeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      val content = awaitItem()
      assertThat(content).isInstanceOf<OnboardingThemeUiState.Content>()
      assertThat((content as OnboardingThemeUiState.Content).selectedTheme).isEqualTo(Theme.SYSTEM_DEFAULT)
    }
  }

  @Test
  fun `selecting a theme persists it and updates the selection`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.Theme)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val settingsDataStore = FakeSettingsDataStore()
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingThemePresenter(sessionStore, navigator, settingsDataStore)

    presenter.test(OnboardingThemeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingThemeEvent.SelectTheme(Theme.DARK))
      runCurrent()
      assertThat(settingsDataStore.theme.value).isEqualTo(Theme.DARK)
      assertThat((awaitItem() as OnboardingThemeUiState.Content).selectedTheme).isEqualTo(Theme.DARK)
    }
  }

  @Test
  fun `continue advances to the next path step`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.Theme)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val settingsDataStore = FakeSettingsDataStore()
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingThemePresenter(sessionStore, navigator, settingsDataStore)

    presenter.test(OnboardingThemeUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingThemeEvent.Continue)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.CoInsured))
    }
  }
}
