package com.hedvig.android.feature.terminateinsurance.step.chooseaction

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.feature.terminateinsurance.TestBackstack
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationResult
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyData
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveyFirstStepKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class TerminationChooseActionPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val commonParams = TerminationGraphParameters(
    contractId = "contractId",
    insuranceDisplayName = "displayName",
    exposureName = "exposure",
    contractGroup = RENTAL,
  )

  private val surveyData = TerminationSurveyData(
    options = emptyList(),
    action = TerminationAction.TerminateWithDate(
      minDate = LocalDate(2026, 11, 25),
      maxDate = LocalDate(2027, 11, 25),
      extraCoverageItems = emptyList(),
    ),
  )

  private val initialState = TerminationChooseActionUiState(
    selectedAction = null,
    isNavigationStepLoading = false,
    navigationStepFailedToLoad = false,
  )

  @Test
  fun `continuing with withdrawal never fetches the termination survey`() = runTest {
    val repository = FakeTerminateInsuranceRepository(surveyData.right())
    val backstack = TestBackstack()
    val presenter = TerminationChooseActionPresenter(commonParams, repository, backstack)
    presenter.test(initialState) {
      skipItems(1)
      sendEvent(TerminationChooseActionEvent.SelectAction(TerminationChooseAction.Withdraw))
      skipItems(1)
      sendEvent(TerminationChooseActionEvent.Continue)
      testScheduler.advanceUntilIdle()
      assertThat(repository.surveyRequests).isEmpty()
      assertThat(backstack.entries).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `continuing with termination fetches the survey and opens its first step`() = runTest {
    val repository = FakeTerminateInsuranceRepository(surveyData.right())
    val backstack = TestBackstack()
    val presenter = TerminationChooseActionPresenter(commonParams, repository, backstack)
    presenter.test(initialState) {
      skipItems(1)
      sendEvent(TerminationChooseActionEvent.SelectAction(TerminationChooseAction.Terminate))
      skipItems(1)
      sendEvent(TerminationChooseActionEvent.Continue)
      testScheduler.advanceUntilIdle()
      assertThat(repository.surveyRequests).isEqualTo(listOf("contractId"))
      assertThat(backstack.entries.single())
        .isInstanceOf<TerminationSurveyFirstStepKey>()
        .prop(TerminationSurveyFirstStepKey::action)
        .isEqualTo(surveyData.action)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a failed survey fetch shows the error and stays on the screen`() = runTest {
    val repository = FakeTerminateInsuranceRepository(ErrorMessage().left())
    val backstack = TestBackstack()
    val presenter = TerminationChooseActionPresenter(commonParams, repository, backstack)
    presenter.test(initialState) {
      skipItems(1)
      sendEvent(TerminationChooseActionEvent.SelectAction(TerminationChooseAction.Terminate))
      skipItems(1)
      sendEvent(TerminationChooseActionEvent.Continue)
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem().navigationStepFailedToLoad).isTrue()
      assertThat(backstack.entries).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }
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
