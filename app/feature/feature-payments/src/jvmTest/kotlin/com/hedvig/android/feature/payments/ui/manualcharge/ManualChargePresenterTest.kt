package com.hedvig.android.feature.payments.ui.manualcharge

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.data.ManualChargeInfoResult
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.test.test
import com.hedvig.android.molecule.test.testFrames
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class ManualChargePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val swish = PayinAccount.SwishPayin(phoneNumber = "0701234567", isPending = false, isDefault = true)

  private val info = ManualChargeInfo(
    chargeId = "missedChargeId",
    missedDueDate = LocalDate(2026, 9, 27),
    amountDue = UiMoney(399.0, UiCurrencyCode.SEK),
    currentMethods = listOf(swish),
    primaryPayinMethod = swish,
    showCancellationWarning = true,
  )

  @Test
  fun `a chargeable missed charge is shown`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    presenter(getInfo).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))
    }
  }

  @Test
  fun `a charge the backend no longer offers is shown as such`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    presenter(getInfo).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.NoLongerChargeable.right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.NoLongerChargeable)
    }
  }

  @Test
  fun `a failed load can be retried`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    presenter(getInfo).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      val offline = ErrorMessage("offline")
      getInfo.responses.send(offline.left())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Failure(offline))
      sendEvent(ManualChargeEvent.Retry)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))
    }
  }

  @Test
  fun `a successful charge ends in the charged state`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      trigger.responses.send(Unit.right())

      assertThat(awaitItem()).isCharged(info)
      assertThat(trigger.calls).isEqualTo(1)
    }
  }

  @Test
  fun `a refresh that lands after the charge keeps the charged state`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      sendEvent(ManualChargeEvent.Refresh)
      this@runTest.testScheduler.advanceUntilIdle()
      trigger.responses.send(Unit.right())
      assertThat(awaitItem()).isCharged(info)

      getInfo.responses.send(ManualChargeInfoResult.NoLongerChargeable.right())
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(getInfo.calls).isEqualTo(2)
      expectNoEvents()
    }
  }

  @Test
  fun `a failed charge shows the error and stays on the screen`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      val insufficientFunds = ErrorMessage("Insufficient funds")
      trigger.responses.send(insufficientFunds.left())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Failure(insufficientFunds))
    }
  }

  @Test
  fun `triggering a charge before the details are loaded does nothing`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      sendEvent(ManualChargeEvent.TriggerCharge)
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(trigger.calls).isEqualTo(0)
      expectNoEvents()
    }
  }

  @Test
  fun `a refresh picks up a newly connected primary method`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trustly = PayinAccount.Trustly("8327", "1234567", "Swedbank", isPending = false, isDefault = true)
    val refreshed = info.copy(currentMethods = listOf(swish, trustly), primaryPayinMethod = trustly)
    presenter(getInfo).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.Refresh)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(refreshed).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(refreshed))
    }
  }

  @Test
  fun `a refresh that cannot reach the backend keeps the details on screen`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    presenter(getInfo).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.Refresh)
      getInfo.responses.send(ErrorMessage("offline").left())
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(getInfo.calls).isEqualTo(2)
      assertThat(expectMostRecentItem()).isEqualTo(ManualChargeUiState.Success(info))
    }
  }

  @Test
  fun `a refresh after the charge was withdrawn stops offering it`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    presenter(getInfo).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.Refresh)
      getInfo.responses.send(ManualChargeInfoResult.NoLongerChargeable.right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.NoLongerChargeable)
    }
  }

  @Test
  fun `a second charge request while one is in flight neither cancels it nor charges again`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      this@runTest.testScheduler.advanceUntilIdle()
      sendEvent(ManualChargeEvent.TriggerCharge)
      this@runTest.testScheduler.advanceUntilIdle()
      trigger.responses.send(Unit.right())

      val charged = awaitItem()
      assertThat(charged).isCharged(info)
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(trigger.calls).isEqualTo(1)
      assertThat(remainingStates() - charged).isEmpty()
    }
  }

  @Test
  fun `two charge requests in a row charge once`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.TriggerCharge)
      sendEvent(ManualChargeEvent.TriggerCharge)
      this@runTest.testScheduler.advanceUntilIdle()
      trigger.responses.send(Unit.right())

      assertThat(expectMostRecentItem()).isCharged(info)
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(trigger.calls).isEqualTo(1)
    }
  }

  @Test
  fun `a charge request after the charge went through does nothing`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))
      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      trigger.responses.send(Unit.right())
      val charged = awaitItem()
      assertThat(charged).isCharged(info)

      sendEvent(ManualChargeEvent.TriggerCharge)
      this@runTest.testScheduler.advanceUntilIdle()

      assertThat(trigger.calls).isEqualTo(1)
      assertThat(remainingStates() - charged).isEmpty()
    }
  }

  @Test
  fun `a retry after the charge went through keeps the charged state without loading`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))
      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      trigger.responses.send(Unit.right())
      val charged = awaitItem()
      assertThat(charged).isCharged(info)

      sendEvent(ManualChargeEvent.Retry)
      this@runTest.testScheduler.advanceUntilIdle()

      assertThat(getInfo.calls).isEqualTo(1)
      assertThat(remainingStates() - charged).isEmpty()
    }
  }

  @Test
  fun `a charge request queued while the presenter was stopped does not charge the stale details`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).restartedWith(
      lastState = ManualChargeUiState.Success(info),
      queued = listOf(ManualChargeEvent.TriggerCharge),
    ) {
      this@runTest.testScheduler.advanceUntilIdle()
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      this@runTest.testScheduler.advanceUntilIdle()

      assertThat(trigger.calls).isEqualTo(0)
      assertThat(expectMostRecentItem()).isEqualTo(ManualChargeUiState.Success(info))
    }
  }

  @Test
  fun `a presenter restarted after the charge went through stays charged without loading`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    val charged = ManualChargeUiState.Charged(NavigationRequest.of(info))
    presenter(getInfo, trigger).restartedWith(lastState = charged) {
      this@runTest.testScheduler.advanceUntilIdle()

      assertThat(expectMostRecentItem()).isEqualTo(charged)
      assertThat(getInfo.calls).isEqualTo(0)
      assertThat(trigger.calls).isEqualTo(0)
    }
  }

  @Test
  fun `a restart after the success screen was opened keeps it opened, so it is not opened again`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).testFrames(ManualChargeUiState.Loading) {
      getInfo.responses.trySend(ManualChargeInfoResult.Chargeable(info).right())
      frame()
      frame()
      emit(ManualChargeEvent.TriggerCharge)
      frame()
      trigger.responses.trySend(Unit.right())
      frame()
      val charged = latest as ManualChargeUiState.Charged
      assertThat(charged.exit.tryConsume()).isTrue()

      restart()
      frame()
      assertThat((latest as ManualChargeUiState.Charged).exit.tryConsume()).isFalse()
      assertThat(trigger.calls).isEqualTo(1)
    }
  }

  @Test
  fun `a restart while charging stops showing the charge and loads the details again`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    presenter(getInfo, trigger).testFrames(ManualChargeUiState.Loading) {
      getInfo.responses.trySend(ManualChargeInfoResult.Chargeable(info).right())
      frame()
      frame()
      emit(ManualChargeEvent.TriggerCharge)
      frame()
      assertThat(latest).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))

      restart()
      frame()
      assertThat(latest).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.trySend(ManualChargeInfoResult.NoLongerChargeable.right())
      frame()
      assertThat(latest).isEqualTo(ManualChargeUiState.NoLongerChargeable)
      assertThat(trigger.calls).isEqualTo(1)
    }
  }

  private fun presenter(
    getInfo: GetManualChargeInfoUseCase,
    trigger: TriggerManualChargeUseCase = FakeTriggerManualChargeUseCase(),
  ) = ManualChargePresenter(getInfo, trigger)
}

/** The distinct states emitted since the last read, which a recomposition may repeat. */
private suspend fun TurbineTestContext<ManualChargeUiState>.remainingStates(): Set<ManualChargeUiState> =
  cancelAndConsumeRemainingEvents().filterIsInstance<Event.Item<ManualChargeUiState>>().map { it.value }.toSet()

/**
 * Runs the presenter as `MoleculeViewModel` restarts it after its collectors left: from [lastState], over a channel
 * that already holds the [queued] events.
 */
private suspend fun ManualChargePresenter.restartedWith(
  lastState: ManualChargeUiState,
  queued: List<ManualChargeEvent> = emptyList(),
  block: suspend TurbineTestContext<ManualChargeUiState>.() -> Unit,
) {
  val events = Channel<ManualChargeEvent>(capacity = 20)
  queued.forEach { events.trySend(it) }
  val scope = MoleculePresenterScope(events.receiveAsFlow())
  moleculeFlow(RecompositionMode.Immediate) {
    with(this@restartedWith) { scope.present(lastState) }
  }.test(validate = block)
}

private class FakeGetManualChargeInfoUseCase : GetManualChargeInfoUseCase {
  val responses = Channel<Either<ErrorMessage, ManualChargeInfoResult>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfoResult> {
    calls++
    return responses.receive()
  }
}

private class FakeTriggerManualChargeUseCase : TriggerManualChargeUseCase {
  val responses = Channel<Either<ErrorMessage, Unit>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, Unit> {
    calls++
    return responses.receive()
  }
}

/** Charged with [info]. The request in it is made by the presenter, so it is not compared. */
private fun Assert<ManualChargeUiState>.isCharged(info: ManualChargeInfo) {
  isInstanceOf<ManualChargeUiState.Charged>().prop(ManualChargeUiState.Charged::manualChargeInfo).isEqualTo(info)
}
