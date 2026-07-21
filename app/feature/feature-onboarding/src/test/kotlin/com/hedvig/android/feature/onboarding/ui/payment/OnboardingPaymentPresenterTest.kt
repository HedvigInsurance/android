package com.hedvig.android.feature.onboarding.ui.payment

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
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

internal class OnboardingPaymentPresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `not connected content when payin methods are missing`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.ConnectPayment)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPaymentPresenter(sessionStore, navigator)

    presenter.test(OnboardingPaymentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData(hasConnectedPayinMethod = false).right())
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingPaymentUiState.Content>()
      val content = state as OnboardingPaymentUiState.Content
      assertThat(content.isConnected).isEqualTo(false)
    }
  }

  @Test
  fun `connect payment pushes the trustly flow`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.ConnectPayment)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPaymentPresenter(sessionStore, navigator)

    presenter.test(OnboardingPaymentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingPaymentEvent.ConnectPayment)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(TrustlyKey)
    }
  }

  @Test
  fun `refresh after returning flips to connected`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.ConnectPayment)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPaymentPresenter(sessionStore, navigator)

    presenter.test(OnboardingPaymentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingPaymentEvent.Refresh)
      runCurrent()
      repository.onboardingDataResponses.add(testOnboardingData(hasConnectedPayinMethod = true).right())
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingPaymentUiState.Content>()
      val content = state as OnboardingPaymentUiState.Content
      assertThat(content.isConnected).isEqualTo(true)
    }
  }

  @Test
  fun `continue advances to BundleDiscount`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.ConnectPayment)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPaymentPresenter(sessionStore, navigator)

    presenter.test(OnboardingPaymentUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      assertThat(awaitItem()).isInstanceOf<OnboardingPaymentUiState.Content>()
      sendEvent(OnboardingPaymentEvent.Continue)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
  }
}
