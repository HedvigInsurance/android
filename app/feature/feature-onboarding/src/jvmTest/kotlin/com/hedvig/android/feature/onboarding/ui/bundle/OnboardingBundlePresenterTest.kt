package com.hedvig.android.feature.onboarding.ui.bundle

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.hedvig.android.core.tracking.test.FakeEventTrackingClient
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
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingBundlePresenter(sessionStore, navigator, FakeEventTrackingClient())

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
  fun `taking a cross-sell offer reports the onboarding surface and the offer url`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val eventTrackingClient = FakeEventTrackingClient()
    val presenter = OnboardingBundlePresenter(sessionStore, navigator, eventTrackingClient)
    val offerUrl =
      "https://www.hedvig.com/api/redirect/recommend?attributed_to=HEDVIG&next=%2Fse-en%2Finsurances%2Faccident&code=accident50app&requiresAuthorization=true"

    presenter.test(OnboardingBundleUiState.Loading) {
      skipItems(1)
      sendEvent(OnboardingBundleEvent.CrossSellClicked("crossSell:ACCIDENT", offerUrl))
      runCurrent()
      assertThat(eventTrackingClient.events).containsExactly(
        FakeEventTrackingClient.TrackedEvent(
          name = "cross_sell_clicked",
          parameters = mapOf(
            "user_flow" to "onboarding",
            "offer_id" to "crossSell:ACCIDENT",
            "campaign_code" to "accident50app",
          ),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `an offer on screen reports the onboarding surface and the offer url`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val eventTrackingClient = FakeEventTrackingClient()
    val presenter = OnboardingBundlePresenter(sessionStore, navigator, eventTrackingClient)
    val offerUrl =
      "https://www.hedvig.com/api/redirect/recommend?attributed_to=HEDVIG&next=%2Fse-en%2Finsurances%2Faccident&code=accident50app&requiresAuthorization=true"

    presenter.test(OnboardingBundleUiState.Loading) {
      skipItems(1)
      sendEvent(OnboardingBundleEvent.CrossSellShown("crossSell:ACCIDENT", offerUrl))
      runCurrent()
      assertThat(eventTrackingClient.events).containsExactly(
        FakeEventTrackingClient.TrackedEvent(
          name = "cross_sell_shown",
          parameters = mapOf(
            "user_flow" to "onboarding",
            "offer_id" to "crossSell:ACCIDENT",
            "campaign_code" to "accident50app",
          ),
        ),
      )
      cancelAndIgnoreRemainingEvents()
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
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, completeOnboarding)
    val presenter = OnboardingBundlePresenter(sessionStore, navigator, FakeEventTrackingClient())

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
