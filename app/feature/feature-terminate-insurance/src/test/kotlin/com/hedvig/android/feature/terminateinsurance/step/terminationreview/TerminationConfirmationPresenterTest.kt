package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup.HOMEOWNER
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationResult
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyData
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.TerminationConfirmation.TerminationType
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
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
    terminationSuccess = null,
    userError = null,
    isSubmittingContractTermination = false,
  )

  @Test
  fun `submit termination calls terminateContract and navigates to success`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      terminateResult = TerminationResult.Terminated(terminationDate).right(),
    )
    val presenter = createPresenter(
      terminationType = TerminationType.Termination(terminationDate),
      repository = repository,
    )
    presenter.test(initialState = initialState(TerminationType.Termination(terminationDate))) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      val states = cancelAndConsumeRemainingEvents().filterIsInstance<app.cash.turbine.Event.Item<OverviewUiState>>()
      val lastState = states.last().value
      assertThat(lastState.terminationSuccess).isNotNull()
      assertThat(lastState.terminationSuccess!!.terminationDate).isEqualTo(terminationDate)
      assertThat(lastState.userError).isNull()
    }
  }

  @Test
  fun `submit deletion calls deleteContract and navigates to success`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      deleteResult = TerminationResult.Deleted.right(),
    )
    val presenter = createPresenter(
      terminationType = TerminationType.Deletion,
      repository = repository,
    )
    presenter.test(initialState = initialState(TerminationType.Deletion)) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      val states = cancelAndConsumeRemainingEvents().filterIsInstance<app.cash.turbine.Event.Item<OverviewUiState>>()
      val lastState = states.last().value
      assertThat(lastState.terminationSuccess).isNotNull()
      assertThat(lastState.terminationSuccess!!.terminationDate).isNull()
    }
  }

  @Test
  fun `submit shows user error when mutation returns UserError`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      terminateResult = TerminationResult.UserError("Cannot terminate this contract").right(),
    )
    val presenter = createPresenter(
      terminationType = TerminationType.Termination(terminationDate),
      repository = repository,
    )
    presenter.test(initialState = initialState(TerminationType.Termination(terminationDate))) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      val states = cancelAndConsumeRemainingEvents().filterIsInstance<app.cash.turbine.Event.Item<OverviewUiState>>()
      val lastState = states.last().value
      assertThat(lastState.userError).isEqualTo("Cannot terminate this contract")
      assertThat(lastState.terminationSuccess).isNull()
      assertThat(lastState.isSubmittingContractTermination).isFalse()
    }
  }

  @Test
  fun `submit shows error message when network call fails`() = runTest {
    val repository = FakeTerminateInsuranceRepository(
      terminateResult = ErrorMessage("Network error").left(),
    )
    val presenter = createPresenter(
      terminationType = TerminationType.Termination(terminationDate),
      repository = repository,
    )
    presenter.test(initialState = initialState(TerminationType.Termination(terminationDate))) {
      skipItems(1)
      sendEvent(TerminationConfirmationEvent.Submit)
      val states = cancelAndConsumeRemainingEvents().filterIsInstance<app.cash.turbine.Event.Item<OverviewUiState>>()
      val lastState = states.last().value
      assertThat(lastState.userError).isEqualTo("Network error")
      assertThat(lastState.terminationSuccess).isNull()
    }
  }

  private fun createPresenter(terminationType: TerminationType, repository: TerminateInsuranceRepository) =
    TerminationConfirmationPresenter(
      terminationType = terminationType,
      insuranceInfo = testInsuranceInfo,
      selectedReasonId = "reason1",
      feedbackComment = null,
      terminateInsuranceRepository = repository,
      getTerminationNotificationUseCase = FakeGetTerminationNotificationUseCase(),
      clock = Clock.System,
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

private class FakeGetTerminationNotificationUseCase :
  com.hedvig.android.feature.terminateinsurance.data.GetTerminationNotificationUseCase(
    apolloClient = com.apollographql.apollo.ApolloClient.Builder().serverUrl("https://unused").build(),
  ) {
  override fun invoke(contractId: String, terminationDate: LocalDate): Flow<Either<ErrorMessage, String?>> {
    return flowOf(null.right())
  }
}
