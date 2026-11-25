package com.hedvig.android.feature.terminateinsurance.step.choose

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.TestBackstack
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationResult
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyData
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationChooseActionKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveyFirstStepKey
import com.hedvig.android.feature.terminateinsurance.navigation.closeTerminationFlow
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test

class ChooseInsuranceToTerminatePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val withdrawable = insurance(id = "withdrawable", supportsWithdrawal = true)
  private val regular = insurance(id = "regular", supportsWithdrawal = false)

  private val surveyData = TerminationSurveyData(
    options = emptyList(),
    action = TerminationAction.TerminateWithDate(
      minDate = LocalDate(2026, 11, 25),
      maxDate = LocalDate(2027, 11, 25),
      extraCoverageItems = emptyList(),
    ),
  )

  @Test
  fun `a known withdrawable contract replaces the choose step with the choose action step`() = runTest {
    val repository = FakeTerminateInsuranceRepository(surveyData.right())
    val backstack = TestBackstack(mutableListOf(ContractDetailTestKey, TerminateInsuranceKey("withdrawable")))
    val presenter = presenter("withdrawable", nonEmptyListOf(withdrawable, regular), repository, backstack)
    presenter.test(ChooseInsuranceToTerminateStepUiState.Loading) {
      testScheduler.advanceUntilIdle()
      assertThat(backstack.entries).containsExactly(
        ContractDetailTestKey,
        TerminationChooseActionKey(TerminationGraphParameters("withdrawable", "name", "exposure", RENTAL)),
      )
      assertThat(repository.surveyRequests).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the only contract replaces the choose step with the survey`() = runTest {
    val repository = FakeTerminateInsuranceRepository(surveyData.right())
    val backstack = TestBackstack(mutableListOf(ContractDetailTestKey, TerminateInsuranceKey(null)))
    val presenter = presenter(null, nonEmptyListOf(regular), repository, backstack)
    presenter.test(ChooseInsuranceToTerminateStepUiState.Loading) {
      testScheduler.advanceUntilIdle()
      assertThat(backstack.entries.size).isEqualTo(2)
      assertThat(backstack.entries.last())
        .isInstanceOf<TerminationSurveyFirstStepKey>()
        .prop(TerminationSurveyFirstStepKey::action)
        .isEqualTo(surveyData.action)
      assertThat(repository.surveyRequests).containsExactly("regular")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `without a contract id and with several contracts the choose step is shown`() = runTest {
    val repository = FakeTerminateInsuranceRepository(surveyData.right())
    val backstack = TestBackstack(mutableListOf(TerminateInsuranceKey(null)))
    val presenter = presenter(null, nonEmptyListOf(withdrawable, regular), repository, backstack)
    presenter.test(ChooseInsuranceToTerminateStepUiState.Loading) {
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem()).isInstanceOf<ChooseInsuranceToTerminateStepUiState.Success>()
      assertThat(backstack.entries).containsExactly(TerminateInsuranceKey(null))
      assertThat(repository.surveyRequests).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a chosen contract is pushed on top of the choose step`() = runTest {
    val repository = FakeTerminateInsuranceRepository(surveyData.right())
    val backstack = TestBackstack(mutableListOf(TerminateInsuranceKey(null)))
    val presenter = presenter(null, nonEmptyListOf(withdrawable, regular), repository, backstack)
    presenter.test(ChooseInsuranceToTerminateStepUiState.Loading) {
      testScheduler.advanceUntilIdle()
      sendEvent(ChooseInsuranceToTerminateEvent.SubmitSelectedInsuranceToTerminate(withdrawable))
      testScheduler.advanceUntilIdle()
      assertThat(backstack.entries.first()).isEqualTo(TerminateInsuranceKey(null))
      assertThat(backstack.entries.last()).isInstanceOf<TerminationChooseActionKey>()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a failed survey for a known contract falls back to the choose step`() = runTest {
    val repository = FakeTerminateInsuranceRepository(ErrorMessage().left())
    val backstack = TestBackstack(mutableListOf(TerminateInsuranceKey("regular")))
    val presenter = presenter("regular", nonEmptyListOf(withdrawable, regular), repository, backstack)
    presenter.test(ChooseInsuranceToTerminateStepUiState.Loading) {
      testScheduler.advanceUntilIdle()
      val state = expectMostRecentItem()
      assertThat(state).isInstanceOf<ChooseInsuranceToTerminateStepUiState.Success>().given {
        assertThat(it.navigationStepFailedToLoad).isTrue()
        assertThat(it.selectedInsurance).isEqualTo(regular)
      }
      assertThat(backstack.entries).containsExactly(TerminateInsuranceKey("regular"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `closing a flow whose choose step was skipped returns to the screen that started it`() = runTest {
    val repository = FakeTerminateInsuranceRepository(surveyData.right())
    val backstack = TestBackstack(mutableListOf(ContractDetailTestKey, TerminateInsuranceKey("withdrawable")))
    val presenter = presenter("withdrawable", nonEmptyListOf(withdrawable), repository, backstack)
    presenter.test(ChooseInsuranceToTerminateStepUiState.Loading) {
      testScheduler.advanceUntilIdle()
      backstack.closeTerminationFlow()
      assertThat(backstack.entries).containsExactly(ContractDetailTestKey)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun presenter(
    insuranceId: String?,
    insurances: NonEmptyList<TerminatableInsurance>,
    repository: TerminateInsuranceRepository,
    backstack: TestBackstack,
  ) = ChooseInsuranceToTerminatePresenter(
    insuranceId = insuranceId,
    getTerminatableContractsUseCase = FakeGetTerminatableContractsUseCase(insurances),
    terminateInsuranceRepository = repository,
    backstack = backstack,
  )

  private fun insurance(id: String, supportsWithdrawal: Boolean) = TerminatableInsurance(
    id = id,
    displayName = "name",
    contractExposure = "exposure",
    contractGroup = RENTAL,
    supportsWithdrawal = supportsWithdrawal,
  )
}

@Serializable
private data object ContractDetailTestKey : HedvigNavKey

private class FakeGetTerminatableContractsUseCase(
  private val insurances: NonEmptyList<TerminatableInsurance>,
) : GetTerminatableContractsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<TerminatableInsurance>?>> =
    flowOf(insurances.right())
}

private class FakeTerminateInsuranceRepository(
  private val surveyResult: Either<ErrorMessage, TerminationSurveyData>,
) : TerminateInsuranceRepository {
  val surveyRequests = mutableListOf<String>()

  override suspend fun getTerminationSurvey(contractId: String): Either<ErrorMessage, TerminationSurveyData> {
    surveyRequests.add(contractId)
    return surveyResult
  }

  override suspend fun terminateContract(
    contractId: String,
    terminationDate: LocalDate,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult> = error("Not used in this test")

  override suspend fun deleteContract(
    contractId: String,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult> = error("Not used in this test")
}
