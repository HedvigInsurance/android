package com.hedvig.android.feature.change.mileage

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageFailureKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSuccessKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSummaryKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSummaryParameters
import com.hedvig.android.feature.change.mileage.ui.summary.ChangeMileageSummaryEvent
import com.hedvig.android.feature.change.mileage.ui.summary.ChangeMileageSummaryPresenter
import com.hedvig.android.feature.change.mileage.ui.summary.ChangeMileageSummaryUiState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChangeMileageSummaryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private data object PreviousScreenKey : HedvigNavKey

  private val params = ChangeMileageSummaryParameters(
    contractDisplayName = "Bilförsäkring",
    contractGroup = ContractGroup.CAR,
    activationDate = testIntent.activationDate,
    currentMonthlyCost = testIntent.currentTotalCost.monthlyNet,
    quote = lowerQuote,
  )
  private val commit = FakeCommitMileageChangeUseCase()
  private val backstack = TestBackstack(
    mutableListOf(PreviousScreenKey, ChangeMileageKey("contractId"), ChangeMileageSummaryKey(params)),
  )
  private val presenter = ChangeMileageSummaryPresenter(params, commit, backstack)

  @Test
  fun `a successful commit replaces the flow with the success screen`() = runTest {
    presenter.test(ChangeMileageSummaryUiState(params, isSubmitting = false)) {
      assertThat(awaitItem()).isEqualTo(ChangeMileageSummaryUiState(params, isSubmitting = false))
      sendEvent(ChangeMileageSummaryEvent.Submit)
      assertThat(awaitItem()).isEqualTo(ChangeMileageSummaryUiState(params, isSubmitting = true))
      commit.turbine.add(Unit.right())
      cancelAndIgnoreRemainingEvents()
      assertThat(backstack.entries).isEqualTo(
        listOf(PreviousScreenKey, ChangeMileageSuccessKey(lowerQuote.displayName, lowerQuote.totalCost.monthlyNet)),
      )
    }
  }

  @Test
  fun `a failed commit shows the failure screen on top of the summary`() = runTest {
    presenter.test(ChangeMileageSummaryUiState(params, isSubmitting = false)) {
      assertThat(awaitItem()).isEqualTo(ChangeMileageSummaryUiState(params, isSubmitting = false))
      sendEvent(ChangeMileageSummaryEvent.Submit)
      assertThat(awaitItem()).isEqualTo(ChangeMileageSummaryUiState(params, isSubmitting = true))
      commit.turbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isEqualTo(ChangeMileageSummaryUiState(params, isSubmitting = false))
      assertThat(backstack.entries.last()).isEqualTo(ChangeMileageFailureKey)
    }
  }
}
