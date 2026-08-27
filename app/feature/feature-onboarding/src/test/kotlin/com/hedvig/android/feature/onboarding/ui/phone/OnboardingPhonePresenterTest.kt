package com.hedvig.android.feature.onboarding.ui.phone

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

internal class OnboardingPhonePresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `content pre-fills the member's phone number`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.PhoneNumber)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPhonePresenter(sessionStore, navigator, repository)

    presenter.test(OnboardingPhoneUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData(phoneNumber = "070 990 12 32").right())
      val content = awaitItem()
      assertThat(content).isInstanceOf<OnboardingPhoneUiState.Content>()
      assertThat((content as OnboardingPhoneUiState.Content).phoneNumber).isEqualTo("070 990 12 32")
    }
  }

  @Test
  fun `save success advances to the next step`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.PhoneNumber)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPhonePresenter(sessionStore, navigator, repository)

    presenter.test(OnboardingPhoneUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingPhoneEvent.Save("0701234567"))
      runCurrent()
      repository.updateContactInfoResponses.add(Unit.right())
      awaitItem() // isSubmitting = true; continueFrom already ran during runCurrent() above
      val settled = awaitItem() // isSubmitting reset to false before navigating away
      assertThat(settled).isInstanceOf<OnboardingPhoneUiState.Content>()
      assertThat((settled as OnboardingPhoneUiState.Content).isSubmitting).isEqualTo(false)
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.Theme))
    }
  }

  @Test
  fun `save failure shows inline error and does not advance`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.PhoneNumber)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPhonePresenter(sessionStore, navigator, repository)

    presenter.test(OnboardingPhoneUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingPhoneEvent.Save("0701234567"))
      runCurrent()
      repository.updateContactInfoResponses.add(ErrorMessage("nope").left())
      awaitItem() // isSubmitting = true
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingPhoneUiState.Content>()
      assertThat((state as OnboardingPhoneUiState.Content).showSubmissionError)
        .isEqualTo(SubmissionError.GeneralError)
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
    }
  }

  @Test
  fun `do this later advances without calling the mutation`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.PhoneNumber)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = testSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPhonePresenter(sessionStore, navigator, repository)

    presenter.test(OnboardingPhoneUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingPhoneEvent.DoThisLater)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.Theme))
      assertThat(repository.updateContactInfoCallCount).isEqualTo(0)
    }
  }
}
