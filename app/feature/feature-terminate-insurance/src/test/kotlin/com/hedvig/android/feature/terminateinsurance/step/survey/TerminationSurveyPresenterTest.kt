package com.hedvig.android.feature.terminateinsurance.step.survey

import app.cash.turbine.Turbine
import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class TerminationSurveyPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val listOfOptionsForHome = listOf(
    TerminationSurveyOption(
      id = "id1",
      feedBackRequired = false,
      title = "I'm moving",
      subOptions = listOf(),
      suggestion = SurveyOptionSuggestion.Action.UpdateAddress,
    ),
    TerminationSurveyOption(
      id = "id2",
      title = " I no longer need insurance",
      feedBackRequired = false,
      suggestion = null,
      subOptions = listOf(
        TerminationSurveyOption("id2-2", "I have moved abroad", feedBackRequired = false, null, listOf()),
        TerminationSurveyOption("id2-1", "Other reason", feedBackRequired = true, null, listOf()),
      ),
    ),
    TerminationSurveyOption(
      id = "id3",
      title = "- I got a better offer elsewhere",
      feedBackRequired = true,
      suggestion = null,
      subOptions = listOf(),
    ),
  )

  @Test
  fun `if tap on feedback field it would open full screen input field`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      assertThat(awaitItem()).isEqualTo(TerminationSurveyState())
//      awaitItem()
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
//      awaitItem()
//      sendEvent(TerminationSurveyEvent.ShowFullScreenEditText(listOfOptionsForHome[3]))
//      assertThat(awaitItem().showFullScreenEditText?.surveyOption).isEqualTo(listOfOptionsForHome[3])
    }
  }
}

private class FakeTerminateInsuranceRepository : TerminateInsuranceRepository {
  val terminationFlowTurbine = Turbine<Either<ErrorMessage, TerminateInsuranceStep>>()

  override suspend fun startTerminationFlow(insuranceId: InsuranceId): Either<ErrorMessage, TerminateInsuranceStep> {
    return terminationFlowTurbine.awaitItem()
  }

  override suspend fun setTerminationDate(terminationDate: LocalDate): Either<ErrorMessage, TerminateInsuranceStep> {
    return terminationFlowTurbine.awaitItem()
  }

  override suspend fun submitReasonForCancelling(
    reason: TerminationReason,
  ): Either<ErrorMessage, TerminateInsuranceStep> {
    return terminationFlowTurbine.awaitItem()
  }

  override suspend fun confirmDeletion(): Either<ErrorMessage, TerminateInsuranceStep> {
    return terminationFlowTurbine.awaitItem()
  }
}
