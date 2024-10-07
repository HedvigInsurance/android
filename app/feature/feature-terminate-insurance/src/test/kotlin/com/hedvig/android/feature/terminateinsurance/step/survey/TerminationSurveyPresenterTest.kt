package com.hedvig.android.feature.terminateinsurance.step.survey

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
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
//
//class TerminationSurveyPresenterTest {
//  @get:Rule
//  val testLogcatLogger = TestLogcatLoggingRule()
//
//  private val listOfOptionsForHome = listOf(
//    TerminationSurveyOption(
//      id = "id1",
//      feedBackRequired = false,
//      title = "I'm moving",
//      subOptions = listOf(),
//      listIndex = 0,
//      suggestion = SurveyOptionSuggestion.Action.UpdateAddress("description", "buttonTitle"),
//    ),
//    TerminationSurveyOption(
//      id = "id2",
//      title = " I no longer need insurance",
//      feedBackRequired = false,
//      suggestion = null,
//      listIndex = 1,
//      subOptions = listOf(
//        TerminationSurveyOption("id2-2", 0, "I have moved abroad", feedBackRequired = false, null, listOf()),
//        TerminationSurveyOption("id2-1", 1, "Other reason", feedBackRequired = true, null, listOf()),
//      ),
//    ),
//    TerminationSurveyOption(
//      id = "id3",
//      title = "- I got a better offer elsewhere",
//      feedBackRequired = true,
//      suggestion = null,
//      listIndex = 2,
//      subOptions = listOf(),
//    ),
//    TerminationSurveyOption(
//      id = "id4",
//      title = "Other reason",
//      feedBackRequired = true,
//      suggestion = null,
//      listIndex = 3,
//      subOptions = listOf(),
//    ),
//  )
//
//  @Test
//  fun `if tap on feedback field it would open full screen input field`() = runTest {
//    val repository = FakeTerminateInsuranceRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      assertThat(awaitItem().reasons.map { it.surveyOption }).isEqualTo(listOfOptionsForHome)
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.ShowFullScreenEditText(listOfOptionsForHome[3]))
//      assertThat(awaitItem().showFullScreenEditText?.surveyOption).isEqualTo(listOfOptionsForHome[3])
//    }
//  }
//
//  @Test
//  fun `if full screen input field is dismissed do not show full screen input field`() = runTest {
//    val repository = FakeTerminateInsuranceRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.ShowFullScreenEditText(listOfOptionsForHome[3]))
//      assertThat(awaitItem().showFullScreenEditText).isNotNull()
//      sendEvent(TerminationSurveyEvent.CloseFullScreenEditText)
//      assertThat(awaitItem().showFullScreenEditText).isNull()
//    }
//  }
//
//  @Test
//  fun `the received options are displayed in the correct order`() = runTest {
//    val repository = FakeTerminateInsuranceRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      assertThat(awaitItem().reasons.map { it.surveyOption }).isEqualTo(listOfOptionsForHome)
//    }
//  }
//
//  @Test
//  fun `if feedback for a reason is changed the right reason is updated with the new feedback`() = runTest {
//    val repository = FakeTerminateInsuranceRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.ChangeFeedbackForSelectedReason("new feedback!"))
//      assertThat(
//        awaitItem().reasons.first { it.surveyOption == listOfOptionsForHome[3] }.feedBack,
//      ).isEqualTo("new feedback!")
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[2]))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.ChangeFeedbackForSelectedReason("new feedback22!"))
//      assertThat(
//        awaitItem()
//          .reasons
//          .first {
//            it.surveyOption == listOfOptionsForHome[2]
//          }.feedBack,
//      ).isEqualTo("new feedback22!")
//    }
//  }
//
//  @Test
//  fun `when survey is submitted the right option with the right feedback is submitted`() = runTest {
//    val repository = FakeTerminateInsuranceRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//    )
//    val nextStep = TerminateInsuranceStep.TerminateInsuranceDate(
//      LocalDate(2024, 6, 1),
//      LocalDate(2024, 6, 29),
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.ChangeFeedbackForSelectedReason("entirely new feedback"))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.Continue)
//      assertThat(
//        awaitItem().navigationStepLoadingForReason,
//      ).isEqualTo(TerminationReason(listOfOptionsForHome[3], "entirely new feedback"))
//      cancelAndIgnoreRemainingEvents()
//    }
//  }
//
//  @Test
//  fun `when survey is submitted for option with no subOptions navigate to nex termination step`() = runTest {
//    val repository = FakeTerminateInsuranceRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//    )
//    val nextStep = TerminateInsuranceStep.TerminateInsuranceDate(
//      LocalDate(2024, 6, 1),
//      LocalDate(2024, 6, 29),
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.Continue)
//      skipItems(1)
//      repository.terminationFlowTurbine.add(nextStep.right())
//      assertThat(awaitItem().nextNavigationStep).isEqualTo(SurveyNavigationStep.NavigateToNextTerminationStep(nextStep))
//    }
//  }
//
//  @Test
//  fun `when survey is submitted for option with subOptions navigate to next survey screen`() = runTest {
//    val repository = FakeTerminateInsuranceRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[1]))
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.Continue)
//      assertThat(awaitItem().nextNavigationStep).isEqualTo(SurveyNavigationStep.NavigateToSubOptions)
//    }
//  }
//}
//
//private class FakeTerminateInsuranceRepository : TerminateInsuranceRepository {
//  val terminationFlowTurbine = Turbine<Either<ErrorMessage, TerminateInsuranceStep>>()
//
//  override suspend fun startTerminationFlow(insuranceId: InsuranceId): Either<ErrorMessage, TerminateInsuranceStep> =
//    terminationFlowTurbine.awaitItem()
//
//  override suspend fun setTerminationDate(terminationDate: LocalDate): Either<ErrorMessage, TerminateInsuranceStep> =
//    terminationFlowTurbine.awaitItem()
//
//  override suspend fun submitReasonForCancelling(
//    reason: TerminationReason,
//  ): Either<ErrorMessage, TerminateInsuranceStep> = terminationFlowTurbine.awaitItem()
//
//  override suspend fun confirmDeletion(): Either<ErrorMessage, TerminateInsuranceStep> =
//    terminationFlowTurbine.awaitItem()
//
//  override suspend fun getContractId(): String {
//    TODO("Not yet implemented")
//  }
//}
