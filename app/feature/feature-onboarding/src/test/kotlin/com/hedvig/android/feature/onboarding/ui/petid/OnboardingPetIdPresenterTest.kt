package com.hedvig.android.feature.onboarding.ui.petid

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
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

internal class OnboardingPetIdPresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class NoopCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    override suspend fun invoke() {}
  }

  @Test
  fun `rows are pinned from the contracts missing pet id at load`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.PetIds)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPetIdPresenter(sessionStore, navigator)

    presenter.test(OnboardingPetIdUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(
        testOnboardingData(
          contracts = listOf(
            OnboardingContract(
              id = "contract-1",
              displayName = "Home Insurance",
              exposureName = "Bellmansgatan 19A",
              typeOfContract = "SE_APARTMENT_RENT",
              missingCoInsuredCount = 0,
              isMissingPetId = true,
            ),
          ),
        ).right(),
      )
      val state = awaitItem()
      assertThat(state).isInstanceOf<OnboardingPetIdUiState.Content>()
      val content = state as OnboardingPetIdUiState.Content
      assertThat(content.rows.size).isEqualTo(1)
      assertThat(content.rows[0].typeOfContract).isEqualTo("SE_APARTMENT_RENT")
      assertThat(content.rows[0].isComplete).isEqualTo(false)
    }
  }

  @Test
  fun `refresh marks completed rows instead of removing them`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.PetIds)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPetIdPresenter(sessionStore, navigator)

    presenter.test(OnboardingPetIdUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(
        testOnboardingData(
          contracts = listOf(
            OnboardingContract(
              id = "contract-1",
              displayName = "Home Insurance",
              exposureName = "Bellmansgatan 19A",
              typeOfContract = "SE_APARTMENT_RENT",
              missingCoInsuredCount = 0,
              isMissingPetId = true,
            ),
          ),
        ).right(),
      )
      awaitItem()
      sendEvent(OnboardingPetIdEvent.Refresh)
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
      assertThat(state).isInstanceOf<OnboardingPetIdUiState.Content>()
      val content = state as OnboardingPetIdUiState.Content
      assertThat(content.rows.size).isEqualTo(1)
      assertThat(content.rows[0].typeOfContract).isEqualTo("SE_APARTMENT_RENT")
      assertThat(content.rows[0].isComplete).isEqualTo(true)
    }
  }

  @Test
  fun `continue advances to InviteFriend`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingStepKey(OnboardingStepId.PetIds)) }
    val repository = FakeOnboardingRepository()
    val sessionStore = OnboardingSessionStore(repository, FakeOnboardingMemberIdProvider())
    val navigator = OnboardingNavigator(backstack, sessionStore, NoopCompleteOnboardingUseCase())
    val presenter = OnboardingPetIdPresenter(sessionStore, navigator)

    presenter.test(OnboardingPetIdUiState.Loading) {
      skipItems(1)
      repository.onboardingDataResponses.add(
        testOnboardingData(
          contracts = listOf(
            OnboardingContract(
              id = "contract-1",
              displayName = "Home Insurance",
              exposureName = "Bellmansgatan 19A",
              typeOfContract = "SE_APARTMENT_RENT",
              missingCoInsuredCount = 0,
              isMissingPetId = true,
            ),
          ),
        ).right(),
      )
      awaitItem()
      sendEvent(OnboardingPetIdEvent.Continue)
      runCurrent()
      assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.InviteFriend))
    }
  }
}
