package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup.HOMEOWNER
import com.hedvig.android.feature.terminateinsurance.FakeGetTerminationNotificationUseCase
import com.hedvig.android.feature.terminateinsurance.TestBackstack
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationResult
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyData
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationConfirmationKey.TerminationType
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationSuccessKey
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSubmitError
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.compose.Backstack
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class TerminationConfirmationPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val testInsuranceInfo = TerminationGraphParameters(
    contractId = "contractId",
    insuranceDisplayName = "Home Insurance",
    exposureName = "Bellmansgatan 19A",
    contractGroup = HOMEOWNER,
  )

  private val terminationDate = LocalDate(2024, 8, 15)

  private fun initialState(terminationType: TerminationType) = OverviewUiState(
    terminationType = terminationType,
    insuranceInfo = testInsuranceInfo,
    extraCoverageItems = emptyList(),
    notificationMessage = null,
    submitError = null,
    isSubmittingContractTermination = false,
  )

  @Test
  fun `submit termination calls terminateContract and navigates to success`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      terminateResult = TerminationResult.Terminated(terminationDate).right(),
    )
    val backstack = TestBackstack(mutableListOf(TerminateInsuranceKey()))
    val scheduler = testScheduler
    val presenter = createPresenter(
      terminationType = TerminationType.Termination(terminationDate),
      repository = repository,
      backstack = backstack,
    )
    presenter.test(initialState = initialState(TerminationType.Termination(terminationDate))) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      scheduler.advanceUntilIdle()
      assertThat(backstack.entries.last())
        .isInstanceOf<TerminationSuccessKey>()
        .prop(TerminationSuccessKey::terminationDate)
        .isEqualTo(terminationDate)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submit deletion calls deleteContract and navigates to success`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      deleteResult = TerminationResult.Deleted.right(),
    )
    val backstack = TestBackstack(mutableListOf(TerminateInsuranceKey()))
    val scheduler = testScheduler
    val presenter = createPresenter(
      terminationType = TerminationType.Deletion,
      repository = repository,
      backstack = backstack,
    )
    presenter.test(initialState = initialState(TerminationType.Deletion)) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      scheduler.advanceUntilIdle()
      assertThat(backstack.entries.last())
        .isInstanceOf<TerminationSuccessKey>()
        .prop(TerminationSuccessKey::terminationDate)
        .isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submit shows user error when mutation returns UserError`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      terminateResult = TerminationResult.UserError("Cannot terminate this contract").right(),
    )
    val backstack = TestBackstack(mutableListOf(TerminateInsuranceKey()))
    val presenter = createPresenter(
      terminationType = TerminationType.Termination(terminationDate),
      repository = repository,
      backstack = backstack,
    )
    presenter.test(initialState = initialState(TerminationType.Termination(terminationDate))) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      val states = cancelAndConsumeRemainingEvents().filterIsInstance<app.cash.turbine.Event.Item<OverviewUiState>>()
      val lastState = states.last().value
      assertThat(lastState.submitError)
        .isEqualTo(TerminationSubmitError.UserError("Cannot terminate this contract"))
      assertThat(lastState.isSubmittingContractTermination).isFalse()
      assertThat(backstack.entries.last()).isInstanceOf<TerminateInsuranceKey>()
    }
  }

  @Test
  fun `submit shows a generic error when the call fails without a message`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      terminateResult = ErrorMessage().left(),
    )
    val backstack = TestBackstack(mutableListOf(TerminateInsuranceKey()))
    val presenter = createPresenter(
      terminationType = TerminationType.Termination(terminationDate),
      repository = repository,
      backstack = backstack,
    )
    presenter.test(initialState = initialState(TerminationType.Termination(terminationDate))) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      val states = cancelAndConsumeRemainingEvents().filterIsInstance<app.cash.turbine.Event.Item<OverviewUiState>>()
      val lastState = states.last().value
      assertThat(lastState.submitError).isEqualTo(TerminationSubmitError.Generic)
      assertThat(backstack.entries.last()).isInstanceOf<TerminateInsuranceKey>()
    }
  }

  @Test
  fun `dismissing the error clears it`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      terminateResult = ErrorMessage().left(),
    )
    val presenter = createPresenter(
      terminationType = TerminationType.Termination(terminationDate),
      repository = repository,
      backstack = TestBackstack(mutableListOf(TerminateInsuranceKey())),
    )
    presenter.test(initialState = initialState(TerminationType.Termination(terminationDate))) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem().submitError).isEqualTo(TerminationSubmitError.Generic)
      sendEvent(TerminationConfirmationEvent.DismissSubmitError)
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem().submitError).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun createPresenter(
    terminationType: TerminationType,
    repository: TerminateInsuranceRepository,
    backstack: Backstack,
  ) = TerminationConfirmationPresenter(
    terminationType = terminationType,
    insuranceInfo = testInsuranceInfo,
    selectedReasonId = "reason1",
    feedbackComment = null,
    terminateInsuranceRepository = repository,
    getTerminationNotificationUseCase = FakeGetTerminationNotificationUseCase(),
    clock = Clock.System,
    backstack = backstack,
  )
}

private class FakeTerminateInsuranceRepository(
  private val terminateResult: Either<ErrorMessage, TerminationResult> =
    TerminationResult.Deleted.right(),
  private val deleteResult: Either<ErrorMessage, TerminationResult> =
    TerminationResult.Deleted.right(),
) : TerminateInsuranceRepository {
  override suspend fun getTerminationSurvey(contractId: String): Either<ErrorMessage, TerminationSurveyData> {
    error("Not used in this test")
  }

  override suspend fun terminateContract(
    contractId: String,
    terminationDate: LocalDate,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult> = terminateResult

  override suspend fun deleteContract(
    contractId: String,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult> = deleteResult
}
