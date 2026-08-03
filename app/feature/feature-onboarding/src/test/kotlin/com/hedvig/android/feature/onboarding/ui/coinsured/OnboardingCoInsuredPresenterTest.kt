package com.hedvig.android.feature.onboarding.ui.coinsured

import androidx.compose.runtime.mutableStateListOf
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingContract
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

internal class OnboardingCoInsuredPresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `rows are pinned from the contracts missing co-insured at load`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.CoInsured)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingCoInsuredPresenter(sessionStore, navigator)

    presenter.test(OnboardingCoInsuredUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingCoInsuredUiState.Content>()
      val content = state as OnboardingCoInsuredUiState.Content
      assertThat(content.rows.size).isEqualTo(1)
      assertThat(content.rows[0].typeOfContract).isEqualTo("SE_APARTMENT_RENT")
      assertThat(content.rows[0].isComplete).isEqualTo(false)
    }
  }

  @Test
  fun `add navigates to the edit co-insured flow`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.CoInsured)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingCoInsuredPresenter(sessionStore, navigator)

    presenter.test(OnboardingCoInsuredUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingCoInsuredEvent.AddCoInsured("contract-1", CoInsuredFlowType.CoInsured))
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(CoInsuredAddInfoKey("contract-1", CoInsuredFlowType.CoInsured))
    }
  }

  @Test
  fun `a co-owners contract yields a co-owners row and navigates into the co-owners flow`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.CoInsured)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingCoInsuredPresenter(sessionStore, navigator)

    presenter.test(OnboardingCoInsuredUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(
        testOnboardingData(
          contracts = listOf(
            OnboardingContract(
              id = "car-1",
              displayName = "Car Insurance",
              exposureName = "ABC 123",
              typeOfContract = "SE_CAR",
              missingCoInsuredCount = 0,
              isMissingPetId = false,
              missingCoOwnersCount = 1,
            ),
          ),
        ).right(),
      )
      val content = awaitItem() as OnboardingCoInsuredUiState.Content
      assertThat(content.rows[0].flowType).isEqualTo(CoInsuredFlowType.CoOwners)
      sendEvent(OnboardingCoInsuredEvent.AddCoInsured("car-1", CoInsuredFlowType.CoOwners))
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(CoInsuredAddInfoKey("car-1", CoInsuredFlowType.CoOwners))
    }
  }

  @Test
  fun `refresh marks completed rows instead of removing them`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.CoInsured)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingCoInsuredPresenter(sessionStore, navigator)

    presenter.test(OnboardingCoInsuredUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingCoInsuredEvent.Refresh)
      runCurrent()
      repository.onboardingDataResponses.add(
        testOnboardingData(
          contracts = listOf(
            OnboardingContract(
              id = "contract-1",
              displayName = "Home Insurance",
              exposureName = "Bellmansgatan 19A",
              typeOfContract = "SE_APARTMENT_RENT",
              missingCoInsuredCount = 0,
              isMissingPetId = false,
            ),
          ),
        ).right(),
      )
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingCoInsuredUiState.Content>()
      val content = state as OnboardingCoInsuredUiState.Content
      assertThat(content.rows.size).isEqualTo(1)
      assertThat(content.rows[0].typeOfContract).isEqualTo("SE_APARTMENT_RENT")
      assertThat(content.rows[0].isComplete).isEqualTo(true)
    }
  }

  @Test
  fun `continue advances to InviteFriend (PetIds skipped for default data)`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.CoInsured)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingCoInsuredPresenter(sessionStore, navigator)

    presenter.test(OnboardingCoInsuredUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(testOnboardingData().right())
      awaitItem()
      sendEvent(OnboardingCoInsuredEvent.Continue)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.InviteFriend))
    }
  }
}
