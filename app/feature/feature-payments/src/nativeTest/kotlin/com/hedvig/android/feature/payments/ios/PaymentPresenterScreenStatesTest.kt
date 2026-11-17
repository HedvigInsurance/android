package com.hedvig.android.feature.payments.ios

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.data.ManualChargeInfoResult
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberChargeShortInfo
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PrimaryPayinMethod
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeEvent
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargePresenter
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState
import com.hedvig.android.feature.payments.ui.payments.PaymentsEvent
import com.hedvig.android.feature.payments.ui.payments.PaymentsPresenter
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlinx.datetime.LocalDate

/**
 * The overview and manual charge presenters, run as their ViewModels run them but with every frame sent at once,
 * mapped to the state Swift receives. Events go through the same guards the screen models apply before sending.
 * The display link of the iOS test process produces no frames, so the screen models themselves only reach Loading.
 */
class PaymentPresenterScreenStatesTest {
  @Test
  fun `the overview fails then a retry shows a member with no upcoming charge who must connect a payin method`() =
    runBlocking {
      val useCase = AnsweringUpcomingPaymentUseCase()
      val run = overviewRun(useCase)

      assertThat(run.awaitState()).isEqualTo(PaymentOverviewScreenState.Loading)
      useCase.responses.send(ErrorMessage().left())
      assertThat(run.awaitState()).isEqualTo(PaymentOverviewScreenState.Failed)
      run.send(PaymentsEvent.Retry)
      assertThat(run.awaitState()).isEqualTo(PaymentOverviewScreenState.Loading)
      useCase.responses.send(
        overview(
          paymentConnection = PaymentConnection.NeedsPayinSetup(LocalDate(2026, 12, 31)),
        ).right(),
      )

      assertThat(run.awaitState()).isEqualTo(
        PaymentOverviewScreenState.Content(
          upcomingCharge = null,
          upcomingChargeNotice = PaymentUpcomingChargeNotice.NoNotice,
          ongoingCharges = emptyList(),
          connectionStatus = PaymentConnectionStatus.NeedsPayinSetup("2026-12-31"),
          primaryPayinMethod = null,
          memberType = PaymentMemberType.STANDARD,
          showsRetryChargeNotice = false,
          anyPayinMethodIsPending = false,
        ),
      )
      assertThat(useCase.calls).isEqualTo(2)
      run.cancel()
    }

  @Test
  fun `a failed last charge shows its period and the amount to pay next to a pending method`() = runBlocking {
    val useCase = AnsweringUpcomingPaymentUseCase()
    val run = overviewRun(useCase)
    assertThat(run.awaitState()).isEqualTo(PaymentOverviewScreenState.Loading)

    useCase.responses.send(
      overview(
        charge = MemberChargeShortInfo(
          netAmount = sek(748.0),
          dueDate = LocalDate(2026, 11, 27),
          id = "upcoming",
          status = MemberCharge.MemberChargeStatus.UPCOMING,
          failedCharge = MemberCharge.FailedCharge(LocalDate(2026, 9, 1), LocalDate(2026, 9, 30), sek(349.0)),
        ),
        ongoingCharges = listOf(PaymentOverview.OngoingCharge("ongoing", LocalDate(2026, 10, 27), sek(120.0))),
        paymentConnection = PaymentConnection.Pending,
        primaryPayinMethod = PrimaryPayinMethod(PayinMethodId.Swish, descriptor = "070-123 45 67", isPending = true),
        isManualChargeAllowed = ManualChargeToPrompt(sek(349.0)),
        showRetryChargeNotice = true,
        anyPayinMethodIsPending = true,
      ).right(),
    )

    assertThat(run.awaitState()).isEqualTo(
      PaymentOverviewScreenState.Content(
        upcomingCharge = PaymentOverviewCharge("upcoming", "2026-11-27", PaymentMoney(748.0, "SEK")),
        upcomingChargeNotice = PaymentUpcomingChargeNotice.ChargeFailed(
          failedPeriodFromDate = "2026-09-01",
          failedPeriodToDate = "2026-09-30",
          manualChargeAmountDue = PaymentMoney(349.0, "SEK"),
        ),
        ongoingCharges = listOf(PaymentOverviewCharge("ongoing", "2026-10-27", PaymentMoney(120.0, "SEK"))),
        connectionStatus = PaymentConnectionStatus.Pending,
        primaryPayinMethod = PaymentPrimaryPayinMethod(PaymentChargeMethod.SWISH, isPending = true),
        memberType = PaymentMemberType.STANDARD,
        showsRetryChargeNotice = true,
        anyPayinMethodIsPending = true,
      ),
    )
    run.cancel()
  }

  @Test
  fun `a pending upcoming charge shows as in progress for a Qasa member who must connect payout`() = runBlocking {
    val useCase = AnsweringUpcomingPaymentUseCase()
    val run = overviewRun(useCase)
    assertThat(run.awaitState()).isEqualTo(PaymentOverviewScreenState.Loading)

    useCase.responses.send(
      overview(
        charge = MemberChargeShortInfo(
          netAmount = sek(399.0),
          dueDate = LocalDate(2026, 11, 27),
          id = "",
          status = MemberCharge.MemberChargeStatus.PENDING,
          failedCharge = MemberCharge.FailedCharge(LocalDate(2026, 9, 1), LocalDate(2026, 9, 30), sek(349.0)),
        ),
        paymentConnection = PaymentConnection.NeedsPayoutSetup,
        memberType = MemberType.QASA_ONLY_MEMBER,
      ).right(),
    )

    assertThat(run.awaitState()).isEqualTo(
      PaymentOverviewScreenState.Content(
        upcomingCharge = PaymentOverviewCharge(null, "2026-11-27", PaymentMoney(399.0, "SEK")),
        upcomingChargeNotice = PaymentUpcomingChargeNotice.InProgress,
        ongoingCharges = emptyList(),
        connectionStatus = PaymentConnectionStatus.NeedsPayoutSetup,
        primaryPayinMethod = null,
        memberType = PaymentMemberType.QASA_ONLY,
        showsRetryChargeNotice = false,
        anyPayinMethodIsPending = false,
      ),
    )
    run.cancel()
  }

  /**
   * The data layer offers a missed charge for manual payment whether or not a future charge exists, but the overview
   * carries the offer only inside the failed charge notice, which needs a future charge. Pins what Swift receives.
   */
  @Test
  fun `a missed charge to pay manually without a future charge reaches the overview nowhere`() = runBlocking {
    val useCase = AnsweringUpcomingPaymentUseCase()
    val run = overviewRun(useCase)
    assertThat(run.awaitState()).isEqualTo(PaymentOverviewScreenState.Loading)

    useCase.responses.send(overview(isManualChargeAllowed = ManualChargeToPrompt(sek(233.0))).right())

    assertThat(run.awaitState()).isEqualTo(
      PaymentOverviewScreenState.Content(
        upcomingCharge = null,
        upcomingChargeNotice = PaymentUpcomingChargeNotice.NoNotice,
        ongoingCharges = emptyList(),
        connectionStatus = PaymentConnectionStatus.Active,
        primaryPayinMethod = null,
        memberType = PaymentMemberType.STANDARD,
        showsRetryChargeNotice = false,
        anyPayinMethodIsPending = false,
      ),
    )
    run.cancel()
  }

  @Test
  fun `a manual charge goes from content through charging to charged once and after it no event is sent`() =
    runBlocking {
      val info = AnsweringManualChargeInfoUseCase()
      val trigger = AnsweringTriggerManualChargeUseCase()
      val run = manualChargeRun(info, trigger)
      assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Loading)
      info.responses.send(ManualChargeInfoResult.Chargeable(manualChargeInfo).right())
      assertThat(run.awaitState()).isEqualTo(manualChargeContent)

      run.triggerCharge()
      assertThat(run.awaitState()).isEqualTo(manualChargeContent.copy(isCharging = true))
      run.triggerCharge()
      run.send(ManualChargeEvent.TriggerCharge)
      run.expectNoState()
      trigger.responses.send(Unit.right())

      val charged = PaymentManualChargeScreenState.Charged(
        showsCancellationWarning = true,
        primaryPayinMethod = PaymentChargeMethod.SWISH,
      )
      assertThat(run.awaitState()).isEqualTo(charged)
      assertThat(charged.triggerChargeEvent()).isNull()
      assertThat(charged.acceptsReload()).isFalse()
      run.send(ManualChargeEvent.Retry)
      run.send(ManualChargeEvent.TriggerCharge)
      run.expectNoState()
      assertThat(trigger.calls).isEqualTo(1)
      assertThat(info.calls).isEqualTo(1)
      run.cancel()
    }

  @Test
  fun `a manual charge that fails to load shows Failed without a message and a retry loads it`() = runBlocking {
    val info = AnsweringManualChargeInfoUseCase()
    val trigger = AnsweringTriggerManualChargeUseCase()
    val run = manualChargeRun(info, trigger)
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Loading)

    info.responses.send(ErrorMessage().left())
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Failed(null))
    run.triggerCharge()
    run.retry()
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Loading)
    info.responses.send(ManualChargeInfoResult.Chargeable(manualChargeInfo).right())

    assertThat(run.awaitState()).isEqualTo(manualChargeContent)
    assertThat(trigger.calls).isEqualTo(0)
    run.cancel()
  }

  @Test
  fun `a declined charge shows the backend's message and a retry offers the charge again`() = runBlocking {
    val info = AnsweringManualChargeInfoUseCase()
    val trigger = AnsweringTriggerManualChargeUseCase()
    val run = manualChargeRun(info, trigger)
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Loading)
    info.responses.send(ManualChargeInfoResult.Chargeable(manualChargeInfo).right())
    assertThat(run.awaitState()).isEqualTo(manualChargeContent)
    run.triggerCharge()
    assertThat(run.awaitState()).isEqualTo(manualChargeContent.copy(isCharging = true))

    trigger.responses.send(ErrorMessage("Insufficient funds").left())
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Failed("Insufficient funds"))
    run.triggerCharge()
    run.retry()
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Loading)
    info.responses.send(ManualChargeInfoResult.Chargeable(manualChargeInfo).right())

    assertThat(run.awaitState()).isEqualTo(manualChargeContent)
    assertThat(trigger.calls).isEqualTo(1)
    assertThat(info.calls).isEqualTo(2)
    run.cancel()
  }

  @Test
  fun `a charge the backend withdrew is no longer chargeable and cannot be charged`() = runBlocking {
    val info = AnsweringManualChargeInfoUseCase()
    val trigger = AnsweringTriggerManualChargeUseCase()
    val run = manualChargeRun(info, trigger)
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.Loading)

    info.responses.send(ManualChargeInfoResult.NoLongerChargeable.right())
    assertThat(run.awaitState()).isEqualTo(PaymentManualChargeScreenState.NoLongerChargeable)
    run.triggerCharge()
    run.send(ManualChargeEvent.TriggerCharge)

    run.expectNoState()
    assertThat(trigger.calls).isEqualTo(0)
    run.cancel()
  }
}

private fun CoroutineScope.overviewRun(useCase: GetUpcomingPaymentUseCase) =
  PresenterRun(this, PaymentsPresenter(useCase), PaymentsUiState.Loading) { it.toScreenState() }

private fun CoroutineScope.manualChargeRun(info: GetManualChargeInfoUseCase, trigger: TriggerManualChargeUseCase) =
  PresenterRun(this, ManualChargePresenter(info, trigger), ManualChargeUiState.Loading) { it.toScreenState() }

/** Sends the charge request as `PaymentManualChargeScreenModel.triggerCharge` does, from the last state received. */
private fun PresenterRun<ManualChargeEvent, *, PaymentManualChargeScreenState>.triggerCharge() {
  last?.triggerChargeEvent()?.let(::send)
}

/** Sends a retry as `PaymentManualChargeScreenModel.retry` does, from the last state received. */
private fun PresenterRun<ManualChargeEvent, *, PaymentManualChargeScreenState>.retry() {
  if (last?.acceptsReload() == true) send(ManualChargeEvent.Retry)
}

/** Runs [presenter] with every frame sent at once and maps each distinct state with [toState]. */
private class PresenterRun<Event, UiState, State>(
  scope: CoroutineScope,
  presenter: MoleculePresenter<Event, UiState>,
  initialState: UiState,
  toState: (UiState) -> State,
) {
  private val events = Channel<Event>(capacity = 20)
  private val states = Channel<State>(Channel.UNLIMITED)
  private val job = scope.launch {
    val presenterScope = MoleculePresenterScope(events.receiveAsFlow())
    moleculeFlow(RecompositionMode.Immediate) { with(presenter) { presenterScope.present(initialState) } }
      .map(toState)
      .distinctUntilChanged()
      .collect { states.send(it) }
  }

  /** The state most recently returned by [awaitState]. */
  var last: State? = null
    private set

  fun send(event: Event) {
    check(events.trySend(event).isSuccess)
  }

  suspend fun awaitState(): State = withTimeout(5.seconds) { states.receive() }.also { last = it }

  suspend fun expectNoState() {
    repeat(50) { yield() }
    assertThat(states.tryReceive().getOrNull()).isNull()
  }

  fun cancel() {
    job.cancel()
  }
}

private fun sek(amount: Double) = UiMoney(amount, UiCurrencyCode.SEK)

private fun overview(
  charge: MemberChargeShortInfo? = null,
  ongoingCharges: List<PaymentOverview.OngoingCharge> = emptyList(),
  paymentConnection: PaymentConnection = PaymentConnection.Active,
  primaryPayinMethod: PrimaryPayinMethod? = null,
  isManualChargeAllowed: ManualChargeToPrompt? = null,
  memberType: MemberType = MemberType.STANDARD_MEMBER,
  showRetryChargeNotice: Boolean = false,
  anyPayinMethodIsPending: Boolean = false,
) = PaymentOverview(
  memberChargeShortInfo = charge,
  ongoingCharges = ongoingCharges,
  paymentConnection = paymentConnection,
  primaryPayinMethod = primaryPayinMethod,
  isManualChargeAllowed = isManualChargeAllowed,
  memberType = memberType,
  showRetryChargeNotice = showRetryChargeNotice,
  anyPayinMethodIsPending = anyPayinMethodIsPending,
)

private val swish = PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = true)
private val pendingTrustly = PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = true, isDefault = false)

private val manualChargeInfo = ManualChargeInfo(
  chargeId = "missed-1",
  missedDueDate = LocalDate(2026, 9, 27),
  amountDue = UiMoney(399.0, UiCurrencyCode.SEK),
  currentMethods = listOf(swish, pendingTrustly),
  primaryPayinMethod = swish,
  showCancellationWarning = true,
)

private val manualChargeContent = PaymentManualChargeScreenState.Content(
  chargeId = "missed-1",
  missedDueDate = "2026-09-27",
  amountDue = PaymentMoney(399.0, "SEK"),
  payinMethods = listOf(
    PaymentConnectedPayinAccount(PaymentPayinAccount.Swish("0701234567"), isPending = false, isDefault = true),
    PaymentConnectedPayinAccount(
      PaymentPayinAccount.Trustly("8327", "91234124", "Swedbank"),
      isPending = true,
      isDefault = false,
    ),
  ),
  primaryPayinAccount = PaymentPayinAccount.Swish("0701234567"),
  showsCancellationWarning = true,
  isCharging = false,
)

private class AnsweringUpcomingPaymentUseCase : GetUpcomingPaymentUseCase {
  val responses = Channel<Either<ErrorMessage, PaymentOverview>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> {
    calls++
    return responses.receive()
  }
}

private class AnsweringManualChargeInfoUseCase : GetManualChargeInfoUseCase {
  val responses = Channel<Either<ErrorMessage, ManualChargeInfoResult>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfoResult> {
    calls++
    return responses.receive()
  }
}

private class AnsweringTriggerManualChargeUseCase : TriggerManualChargeUseCase {
  val responses = Channel<Either<ErrorMessage, Unit>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, Unit> {
    calls++
    return responses.receive()
  }
}
