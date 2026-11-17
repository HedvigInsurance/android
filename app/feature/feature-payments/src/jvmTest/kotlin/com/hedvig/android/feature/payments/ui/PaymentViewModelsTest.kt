package com.hedvig.android.feature.payments.ui

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payments.data.GetChargeDetailsUseCase
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.data.ManualChargeInfoResult
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberPaymentChargeMethod
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentDetails
import com.hedvig.android.feature.payments.data.PaymentHistoryItem
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsEvent
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsUiState
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryEvent
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryUiState
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeEvent
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeViewModel
import com.hedvig.android.feature.payments.ui.payments.PaymentsEvent
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Drives the ViewModels each Swift screen model owns, with their real presenters and the JVM frame clock, through the
 * events the screen models send: a load that fails and then succeeds after the screen model's retry or reload, and a
 * load or charge still in flight when the screen model's close clears the ViewModel.
 */
class PaymentViewModelsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val store = ViewModelStore()

  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    store.clear()
    Dispatchers.resetMain()
  }

  @Test
  fun `history fails, then a reload loads the charges`() = runTest {
    val useCase = FakeHistoryUseCase()
    val viewModel = PaymentHistoryViewModel(useCase).also { store.put("vm", it) }

    viewModel.uiState.test {
      assertThat(awaitItem()).isEqualTo(PaymentHistoryUiState.Loading)
      useCase.responses.send(ErrorMessage().left())
      assertThat(awaitItem()).isEqualTo(PaymentHistoryUiState.Failure)
      viewModel.emit(PaymentHistoryEvent.Reload)
      assertThat(awaitItem()).isEqualTo(PaymentHistoryUiState.Loading)
      useCase.responses.send(historyItems.right())
      assertThat(awaitItem()).isEqualTo(PaymentHistoryUiState.Success(historyItems))
    }
    assertThat(useCase.calls).isEqualTo(2)
  }

  @Test
  fun `details fail, then a reload loads the same charge`() = runTest {
    val useCase = FakeDetailsUseCase()
    val viewModel = PaymentDetailsViewModel("charge-1", useCase).also { store.put("vm", it) }

    viewModel.uiState.test {
      assertThat(awaitItem()).isEqualTo(PaymentDetailsUiState.Loading)
      useCase.responses.send(ErrorMessage().left())
      assertThat(awaitItem()).isEqualTo(PaymentDetailsUiState.Failure)
      viewModel.emit(PaymentDetailsEvent.Reload)
      assertThat(awaitItem()).isEqualTo(PaymentDetailsUiState.Loading)
      useCase.responses.send(details.right())
      assertThat(awaitItem()).isEqualTo(PaymentDetailsUiState.Success(details))
    }
    assertThat(useCase.ids).containsExactly("charge-1", "charge-1")
  }

  @Test
  fun `the overview fails, then a retry loads it`() = runTest {
    val useCase = FakeOverviewUseCase()
    val viewModel = PaymentsViewModel(useCase).also { store.put("vm", it) }

    viewModel.uiState.test {
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      useCase.responses.send(ErrorMessage().left())
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Error)
      viewModel.emit(PaymentsEvent.Retry)
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      useCase.responses.send(overview.right())
      assertThat(awaitItem()).isEqualTo(
        PaymentsUiState.Content(
          isRetrying = false,
          upcomingPayment = PaymentsUiState.Content.UpcomingPayment.NoUpcomingPayment,
          upcomingPaymentInfo = PaymentsUiState.Content.UpcomingPaymentInfo.NoInfo,
          ongoingCharges = emptyList(),
          connectedPaymentInfo = PaymentsUiState.Content.ConnectedPaymentInfo.NeedsPayinSetup(null),
          primaryPayinMethod = null,
          memberType = MemberType.STANDARD_MEMBER,
          showRetryChargeNotice = false,
          anyPayinMethodIsPending = false,
        ),
      )
    }
    assertThat(useCase.calls).isEqualTo(2)
  }

  @Test
  fun `the manual charge fails to load, then a retry loads it without charging`() = runTest {
    val info = FakeManualChargeInfoUseCase()
    val trigger = FakeTriggerUseCase()
    val viewModel = ManualChargeViewModel(info, trigger).also { store.put("vm", it) }

    viewModel.uiState.test {
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      val offline = ErrorMessage()
      info.responses.send(offline.left())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Failure(offline))
      viewModel.emit(ManualChargeEvent.Retry)
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Loading)
      info.responses.send(ManualChargeInfoResult.Chargeable(manualChargeInfo).right())
      assertThat(awaitItem()).isEqualTo(ManualChargeUiState.Success(manualChargeInfo))
    }
    assertThat(info.calls).isEqualTo(2)
    assertThat(trigger.calls).isEqualTo(0)
  }

  @Test
  fun `a history load that finishes after the ViewModel is cleared is not delivered`() = runTest {
    val useCase = FakeHistoryUseCase()
    val states = collect(PaymentHistoryViewModel(useCase))

    answerAfterClearing(useCase, historyItems.right())

    assertThat(states).containsExactly(PaymentHistoryUiState.Loading)
    assertThat(useCase.cancellations).isEqualTo(1)
  }

  @Test
  fun `a details load that finishes after the ViewModel is cleared is not delivered`() = runTest {
    val useCase = FakeDetailsUseCase()
    val states = collect(PaymentDetailsViewModel("charge-1", useCase))

    answerAfterClearing(useCase, details.right())

    assertThat(states).containsExactly(PaymentDetailsUiState.Loading)
    assertThat(useCase.cancellations).isEqualTo(1)
  }

  @Test
  fun `an overview load that finishes after the ViewModel is cleared is not delivered`() = runTest {
    val useCase = FakeOverviewUseCase()
    val states = collect(PaymentsViewModel(useCase))

    answerAfterClearing(useCase, overview.right())

    assertThat(states).containsExactly(PaymentsUiState.Loading)
    assertThat(useCase.cancellations).isEqualTo(1)
  }

  @Test
  fun `a manual charge load that finishes after the ViewModel is cleared is not delivered`() = runTest {
    val info = FakeManualChargeInfoUseCase()
    val states = collect(ManualChargeViewModel(info, FakeTriggerUseCase()))

    answerAfterClearing(info, ManualChargeInfoResult.Chargeable(manualChargeInfo).right())

    assertThat(states).containsExactly(ManualChargeUiState.Loading)
    assertThat(info.cancellations).isEqualTo(1)
  }

  @Test
  fun `clearing the ViewModel while a charge is in flight cancels it, and Charged never arrives`() = runTest {
    val info = FakeManualChargeInfoUseCase()
    val trigger = FakeTriggerUseCase()
    val viewModel = ManualChargeViewModel(info, trigger)
    val states = collect(viewModel)
    info.responses.send(ManualChargeInfoResult.Chargeable(manualChargeInfo).right())
    advanceUntilIdle()
    viewModel.emit(ManualChargeEvent.TriggerCharge)
    advanceUntilIdle()

    answerAfterClearing(trigger, Unit.right())

    assertThat(states).containsExactly(
      ManualChargeUiState.Loading,
      ManualChargeUiState.Success(manualChargeInfo),
      ManualChargeUiState.Success(manualChargeInfo, payButtonLoading = true),
    )
    assertThat(trigger.calls).isEqualTo(1)
    assertThat(trigger.cancellations).isEqualTo(1)
  }

  /** Puts [viewModel] in the store and collects its state until the test ends, as one Swift collection does. */
  private fun <S> TestScope.collect(viewModel: MoleculeViewModel<*, S>): List<S> {
    store.put("vm", viewModel)
    val states = mutableListOf<S>()
    backgroundScope.launch { viewModel.uiState.collect { states += it } }
    advanceUntilIdle()
    return states
  }

  /** Clears the store while [useCase] is suspended in a call, then answers that call. */
  private suspend fun <T> TestScope.answerAfterClearing(useCase: FakeUseCase<T>, answer: T) {
    assertThat(useCase.calls - useCase.cancellations).isEqualTo(1)
    store.clear()
    useCase.responses.send(answer)
    advanceUntilIdle()
  }
}

private val historyItems = listOf(
  PaymentHistoryItem(
    UiMoney(449.5, UiCurrencyCode.SEK),
    "charge-2",
    MemberCharge.MemberChargeStatus.FAILED,
    LocalDate(2026, 2, 27),
  ),
  PaymentHistoryItem(
    UiMoney(399.0, UiCurrencyCode.SEK),
    "",
    MemberCharge.MemberChargeStatus.SUCCESS,
    LocalDate(2026, 1, 27),
  ),
)

private val details = PaymentDetails(
  memberCharge = MemberCharge(
    grossAmount = UiMoney(399.0, UiCurrencyCode.SEK),
    netAmount = UiMoney(399.0, UiCurrencyCode.SEK),
    id = "charge-1",
    status = MemberCharge.MemberChargeStatus.SUCCESS,
    dueDate = LocalDate(2026, 1, 27),
    failedCharge = null,
    chargeBreakdowns = emptyList(),
    referralDiscount = null,
    carriedAdjustment = null,
    settlementAdjustment = null,
    chargeMethod = MemberPaymentChargeMethod.SWISH,
  ),
  pastCharges = emptyList(),
  paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
  upComingCharge = null,
)

private val overview = PaymentOverview(
  memberChargeShortInfo = null,
  ongoingCharges = emptyList(),
  paymentConnection = PaymentConnection.NeedsPayinSetup(null),
  primaryPayinMethod = null,
  isManualChargeAllowed = null,
  memberType = MemberType.STANDARD_MEMBER,
  showRetryChargeNotice = false,
  anyPayinMethodIsPending = false,
)

private val swish = PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = true)

private val manualChargeInfo = ManualChargeInfo(
  chargeId = "missed-1",
  missedDueDate = LocalDate(2026, 9, 27),
  amountDue = UiMoney(399.0, UiCurrencyCode.SEK),
  currentMethods = listOf(swish),
  primaryPayinMethod = swish,
  showCancellationWarning = false,
)

/** Answers each call with the next of [responses], and counts calls and the calls cancelled while waiting. */
private abstract class FakeUseCase<T> {
  val responses = Channel<T>(Channel.UNLIMITED)
  var calls = 0
  var cancellations = 0

  protected suspend fun answer(): T {
    calls++
    try {
      return responses.receive()
    } catch (e: CancellationException) {
      cancellations++
      throw e
    }
  }
}

private class FakeHistoryUseCase :
  FakeUseCase<Either<ErrorMessage, List<PaymentHistoryItem>>>(),
  GetPaymentsHistoryUseCase {
  override suspend fun invoke() = answer()
}

private class FakeDetailsUseCase :
  FakeUseCase<Either<ErrorMessage, PaymentDetails>>(),
  GetChargeDetailsUseCase {
  val ids = mutableListOf<String?>()

  override suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails> {
    ids += id
    return answer()
  }
}

private class FakeOverviewUseCase :
  FakeUseCase<Either<ErrorMessage, PaymentOverview>>(),
  GetUpcomingPaymentUseCase {
  override suspend fun invoke() = answer()
}

private class FakeManualChargeInfoUseCase :
  FakeUseCase<Either<ErrorMessage, ManualChargeInfoResult>>(),
  GetManualChargeInfoUseCase {
  override suspend fun invoke() = answer()
}

private class FakeTriggerUseCase :
  FakeUseCase<Either<ErrorMessage, Unit>>(),
  TriggerManualChargeUseCase {
  override suspend fun invoke() = answer()
}
