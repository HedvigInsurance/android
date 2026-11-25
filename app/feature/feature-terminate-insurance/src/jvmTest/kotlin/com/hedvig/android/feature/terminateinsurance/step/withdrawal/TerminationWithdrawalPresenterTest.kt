package com.hedvig.android.feature.terminateinsurance.step.withdrawal

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.feature.terminateinsurance.FakeContractWithdrawalRepository
import com.hedvig.android.feature.terminateinsurance.TestBackstack
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.WithdrawalResult
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationWithdrawalKey
import com.hedvig.android.feature.terminateinsurance.navigation.WithdrawalSuccessKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test

class TerminationWithdrawalPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val commonParams = TerminationGraphParameters("contractId", "name", "exposure", RENTAL)
  private val today = LocalDate(2026, 11, 25)
  private val endsToday = TerminationAction.TerminateWithDate(today, today, emptyList())

  private fun initialState(action: TerminationAction) = TerminationWithdrawalUiState(
    insuranceInfo = commonParams,
    statusText = "status",
    action = action,
    endDate = today,
    userError = null,
    isSubmitting = false,
  )

  private fun flowBackstack(action: TerminationAction) = TestBackstack(
    mutableListOf(StartingTestKey, TerminationWithdrawalKey(commonParams, "status", action)),
  )

  @Test
  fun `a withdrawn contract replaces the flow with success carrying its end date`() = runTest {
    val repository = FakeContractWithdrawalRepository(withdrawResult = WithdrawalResult.Withdrawn.right())
    val backstack = flowBackstack(endsToday)
    TerminationWithdrawalPresenter("contractId", repository, backstack).test(initialState(endsToday)) {
      skipItems(1)
      sendEvent(TerminationWithdrawalEvent.Submit)
      testScheduler.advanceUntilIdle()
      assertThat(repository.withdrawRequests).containsExactly("contractId")
      assertThat(backstack.entries).containsExactly(StartingTestKey, WithdrawalSuccessKey(today))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `an active contract ends on the date the backend gives`() {
    val tomorrow = LocalDate(2026, 11, 26)
    val action = TerminationAction.TerminateWithDate(tomorrow, tomorrow, emptyList())
    assertThat(withdrawalEndDate(action, fixedClockAt(today))).isEqualTo(tomorrow)
  }

  @Test
  fun `a contract that has not started yet ends today`() {
    val deletion = TerminationAction.DeleteInsurance(emptyList())
    assertThat(withdrawalEndDate(deletion, fixedClockAt(today))).isEqualTo(today)
  }

  private fun fixedClockAt(date: LocalDate): Clock = object : Clock {
    override fun now(): Instant = date.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
  }

  @Test
  fun `a user error is shown and the member stays on the screen`() = runTest {
    val repository = FakeContractWithdrawalRepository(withdrawResult = WithdrawalResult.UserError("nope").right())
    val backstack = flowBackstack(endsToday)
    TerminationWithdrawalPresenter("contractId", repository, backstack).test(initialState(endsToday)) {
      skipItems(1)
      sendEvent(TerminationWithdrawalEvent.Submit)
      testScheduler.advanceUntilIdle()
      val state = expectMostRecentItem()
      assertThat(state.userError).isEqualTo("nope")
      assertThat(state.isSubmitting).isFalse()
      assertThat(backstack.entries.size).isEqualTo(2)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a network error is shown and the member stays on the screen`() = runTest {
    val repository = FakeContractWithdrawalRepository(withdrawResult = ErrorMessage("offline").left())
    val backstack = flowBackstack(endsToday)
    TerminationWithdrawalPresenter("contractId", repository, backstack).test(initialState(endsToday)) {
      skipItems(1)
      sendEvent(TerminationWithdrawalEvent.Submit)
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem().userError).isEqualTo("offline")
      assertThat(backstack.entries.size).isEqualTo(2)
      cancelAndIgnoreRemainingEvents()
    }
  }
}

@Serializable
private data object StartingTestKey : HedvigNavKey
