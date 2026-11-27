package com.hedvig.android.feature.terminateinsurance.step.withdrawal

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.feature.terminateinsurance.FakeContractWithdrawalRepository
import com.hedvig.android.feature.terminateinsurance.TestBackstack
import com.hedvig.android.feature.terminateinsurance.data.ContractWithdrawalData
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.WithdrawalResult
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationWithdrawalKey
import com.hedvig.android.feature.terminateinsurance.navigation.WithdrawalSuccessKey
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSubmitError
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
  private val withdrawalData = ContractWithdrawalData(
    statusText = "status",
    action = endsToday,
    confirmationEmail = "member@example.com",
  )

  private fun flowBackstack() = TestBackstack(mutableListOf(StartingTestKey, TerminationWithdrawalKey(commonParams)))

  private fun presenter(repository: FakeContractWithdrawalRepository, backstack: TestBackstack) =
    TerminationWithdrawalPresenter(commonParams, repository, fixedClockAt(today), backstack)

  private fun repository(
    withdrawalData: Either<ErrorMessage, ContractWithdrawalData> = this.withdrawalData.right(),
    withdrawResult: Either<ErrorMessage, WithdrawalResult> = ErrorMessage().left(),
  ) = FakeContractWithdrawalRepository(withdrawalData = withdrawalData, withdrawResult = withdrawResult)

  @Test
  fun `the screen loads its withdrawal details and the member's email`() = runTest {
    val repository = repository()
    presenter(repository, flowBackstack()).test(TerminationWithdrawalUiState.Loading) {
      testScheduler.advanceUntilIdle()
      assertThat(repository.withdrawalRequests).containsExactly("contractId")
      assertThat(expectMostRecentItem()).isEqualTo(
        TerminationWithdrawalUiState.Content(
          insuranceInfo = commonParams,
          statusText = "status",
          action = endsToday,
          endDate = today,
          confirmationEmail = "member@example.com",
          submitError = null,
          isSubmitting = false,
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a failed load shows the failure state and retry loads again`() = runTest {
    val repository = repository(withdrawalData = ErrorMessage().left())
    presenter(repository, flowBackstack()).test(TerminationWithdrawalUiState.Loading) {
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem()).isEqualTo(TerminationWithdrawalUiState.Failure)
      sendEvent(TerminationWithdrawalEvent.Retry)
      testScheduler.advanceUntilIdle()
      assertThat(repository.withdrawalRequests).containsExactly("contractId", "contractId")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a refresh picks up an email changed elsewhere without showing loading`() = runTest {
    val repository = repository()
    presenter(repository, flowBackstack()).test(TerminationWithdrawalUiState.Loading) {
      testScheduler.advanceUntilIdle()
      expectMostRecentItem()
      repository.withdrawalData = withdrawalData.copy(confirmationEmail = "new@example.com").right()
      sendEvent(TerminationWithdrawalEvent.Refresh)
      testScheduler.advanceUntilIdle()
      assertThat(repository.withdrawalRequests).containsExactly("contractId", "contractId")
      assertThat(expectMostRecentItem()).isInstanceOf<TerminationWithdrawalUiState.Content>().given {
        assertThat(it.confirmationEmail).isEqualTo("new@example.com")
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a failed refresh keeps the content already shown`() = runTest {
    val repository = repository()
    presenter(repository, flowBackstack()).test(TerminationWithdrawalUiState.Loading) {
      testScheduler.advanceUntilIdle()
      val loaded = expectMostRecentItem()
      repository.withdrawalData = ErrorMessage().left()
      sendEvent(TerminationWithdrawalEvent.Refresh)
      testScheduler.advanceUntilIdle()
      assertThat(repository.withdrawalRequests).containsExactly("contractId", "contractId")
      assertThat(loaded).isInstanceOf<TerminationWithdrawalUiState.Content>()
      assertThat(expectMostRecentItem()).isEqualTo(loaded)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a withdrawn contract replaces the flow with success carrying its end date`() = runTest {
    val repository = repository(withdrawResult = WithdrawalResult.Withdrawn.right())
    val backstack = flowBackstack()
    presenter(repository, backstack).test(TerminationWithdrawalUiState.Loading) {
      testScheduler.advanceUntilIdle()
      sendEvent(TerminationWithdrawalEvent.Submit)
      testScheduler.advanceUntilIdle()
      assertThat(repository.withdrawRequests).containsExactly("contractId")
      assertThat(backstack.entries).containsExactly(StartingTestKey, WithdrawalSuccessKey(today))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a user error is shown and the member stays on the screen`() = runTest {
    val repository = repository(withdrawResult = WithdrawalResult.UserError("nope").right())
    val backstack = flowBackstack()
    presenter(repository, backstack).test(TerminationWithdrawalUiState.Loading) {
      testScheduler.advanceUntilIdle()
      sendEvent(TerminationWithdrawalEvent.Submit)
      testScheduler.advanceUntilIdle()
      val state = expectMostRecentItem()
      assertThat(state).isInstanceOf<TerminationWithdrawalUiState.Content>().given {
        assertThat(it.submitError).isEqualTo(TerminationSubmitError.UserError("nope"))
        assertThat(it.isSubmitting).isFalse()
      }
      assertThat(backstack.entries.size).isEqualTo(2)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a failure without a message shows a generic error that can be dismissed`() = runTest {
    val backstack = flowBackstack()
    presenter(repository(), backstack).test(TerminationWithdrawalUiState.Loading) {
      testScheduler.advanceUntilIdle()
      sendEvent(TerminationWithdrawalEvent.Submit)
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem()).isInstanceOf<TerminationWithdrawalUiState.Content>().given {
        assertThat(it.submitError).isEqualTo(TerminationSubmitError.Generic)
        assertThat(it.isSubmitting).isFalse()
      }
      sendEvent(TerminationWithdrawalEvent.DismissSubmitError)
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem()).isInstanceOf<TerminationWithdrawalUiState.Content>().given {
        assertThat(it.submitError).isNull()
      }
      assertThat(backstack.entries.size).isEqualTo(2)
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
}

@Serializable
private data object StartingTestKey : HedvigNavKey
