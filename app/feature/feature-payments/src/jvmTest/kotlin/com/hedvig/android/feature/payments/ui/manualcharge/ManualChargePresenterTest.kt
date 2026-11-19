package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.runtime.mutableStateListOf
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.data.ManualChargeInfoResult
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.feature.payments.navigation.ManualChargeKey
import com.hedvig.android.feature.payments.navigation.ManualChargeSuccessKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.channels.Channel
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
  fun `a successful charge replaces the screen with the success screen`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    val backstack = TestBackstack(TestRootKey, ManualChargeKey)
    presenter(getInfo, trigger, backstack).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      trigger.responses.send(Unit.right())
      this@runTest.testScheduler.advanceUntilIdle()

      assertThat(trigger.calls).isEqualTo(1)
      assertThat(backstack.entries).containsExactly(
        TestRootKey,
        ManualChargeSuccessKey(showCancellationWarning = true, primaryPayinProvider = PaymentProvider.Swish),
      )
    }
  }

  @Test
  fun `a failed charge shows the error and stays on the screen`() = runTest {
    val getInfo = FakeGetManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    val backstack = TestBackstack(TestRootKey, ManualChargeKey)
    presenter(getInfo, trigger, backstack).test(ManualChargeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      getInfo.responses.send(ManualChargeInfoResult.Chargeable(info).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info))

      sendEvent(ManualChargeEvent.TriggerCharge)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(info, payButtonLoading = true))
      val insufficientFunds = ErrorMessage("Insufficient funds")
      trigger.responses.send(insufficientFunds.left())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Failure(insufficientFunds))
      assertThat(backstack.entries).containsExactly(TestRootKey, ManualChargeKey)
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
      assertThat(expectMostRecentItem()).isEqualTo(ManualChargeUiState.Loading)
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

  private fun presenter(
    getInfo: GetManualChargeInfoUseCase,
    trigger: TriggerManualChargeUseCase = FakeTriggerManualChargeUseCase(),
    backstack: Backstack = TestBackstack(TestRootKey, ManualChargeKey),
  ) = ManualChargePresenter(getInfo, trigger, backstack)
}

private data object TestRootKey : HedvigNavKey

private class TestBackstack(vararg initial: HedvigNavKey) : Backstack {
  override val entries: MutableList<HedvigNavKey> = mutableStateListOf(*initial)
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
