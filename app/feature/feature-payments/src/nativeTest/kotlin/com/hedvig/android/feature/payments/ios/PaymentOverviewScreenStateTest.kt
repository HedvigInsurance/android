package com.hedvig.android.feature.payments.ios

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.data.PrimaryPayinMethod
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPayment
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class PaymentOverviewScreenStateTest {
  @Test
  fun `Loading maps to Loading`() {
    assertThat(PaymentsUiState.Loading.toScreenState()).isEqualTo(PaymentOverviewScreenState.Loading)
  }

  @Test
  fun `Error maps to Failed`() {
    assertThat(PaymentsUiState.Error.toScreenState()).isEqualTo(PaymentOverviewScreenState.Failed)
  }

  @Test
  fun `content maps every field`() {
    val state = content(
      upcomingPayment = UpcomingPayment.Content(UiMoney(5552.25, UiCurrencyCode.SEK), LocalDate(2026, 11, 27), "c-1"),
      upcomingPaymentInfo = UpcomingPaymentInfo.InProgress,
      ongoingCharges = listOf(
        OngoingCharge("ongoing-2", LocalDate(2026, 10, 27), UiMoney(100.0, UiCurrencyCode.NOK)),
        OngoingCharge("ongoing-1", LocalDate(2026, 9, 5), UiMoney(-20.5, UiCurrencyCode.SEK)),
      ),
      connectedPaymentInfo = ConnectedPaymentInfo.Pending,
      primaryPayinMethod = PrimaryPayinMethod(PayinMethodId.Swish, "070-123 45 67", isPending = true),
      memberType = MemberType.STANDARD_TO_QASA_MEMBER,
      showRetryChargeNotice = true,
      anyPayinMethodIsPending = true,
    )

    assertThat(state.toScreenState()).isEqualTo(
      PaymentOverviewScreenState.Content(
        upcomingCharge = PaymentOverviewCharge("c-1", "2026-11-27", PaymentMoney(5552.25, "SEK")),
        upcomingChargeNotice = PaymentUpcomingChargeNotice.InProgress,
        ongoingCharges = listOf(
          PaymentOverviewCharge("ongoing-2", "2026-10-27", PaymentMoney(100.0, "NOK")),
          PaymentOverviewCharge("ongoing-1", "2026-09-05", PaymentMoney(-20.5, "SEK")),
        ),
        connectionStatus = PaymentConnectionStatus.Pending,
        primaryPayinMethod = PaymentPrimaryPayinMethod(PaymentChargeMethod.SWISH, isPending = true),
        memberType = PaymentMemberType.STANDARD_TO_QASA,
        showsRetryChargeNotice = true,
        anyPayinMethodIsPending = true,
      ),
    )
  }

  @Test
  fun `no upcoming payment maps to a null upcoming charge`() {
    val mapped = content(upcomingPayment = UpcomingPayment.NoUpcomingPayment).toScreenState()

    assertThat((mapped as PaymentOverviewScreenState.Content).upcomingCharge).isNull()
  }

  @Test
  fun `an upcoming charge without an id or with an empty id has a null id`() {
    val ids = listOf(null, "").map { id ->
      val payment = UpcomingPayment.Content(UiMoney(1.0, UiCurrencyCode.SEK), LocalDate(2026, 1, 1), id)
      (content(upcomingPayment = payment).toScreenState() as PaymentOverviewScreenState.Content).upcomingCharge?.id
    }

    assertThat(ids).containsExactly(null, null)
  }

  @Test
  fun `each upcoming payment info maps to its notice`() {
    val infos = listOf(
      UpcomingPaymentInfo.NoInfo,
      UpcomingPaymentInfo.InProgress,
      UpcomingPaymentInfo.PaymentFailed(LocalDate(2026, 9, 1), LocalDate(2026, 9, 30), isManualChargeAllowed = null),
      UpcomingPaymentInfo.PaymentFailed(
        LocalDate(2026, 9, 1),
        LocalDate(2026, 9, 30),
        ManualChargeToPrompt(UiMoney(399.0, UiCurrencyCode.DKK)),
      ),
    )

    val notices = infos.map {
      (content(upcomingPaymentInfo = it).toScreenState() as PaymentOverviewScreenState.Content).upcomingChargeNotice
    }

    assertThat(notices).containsExactly(
      PaymentUpcomingChargeNotice.NoNotice,
      PaymentUpcomingChargeNotice.InProgress,
      PaymentUpcomingChargeNotice.ChargeFailed("2026-09-01", "2026-09-30", manualChargeAmountDue = null),
      PaymentUpcomingChargeNotice.ChargeFailed("2026-09-01", "2026-09-30", PaymentMoney(399.0, "DKK")),
    )
  }

  @Test
  fun `each connected payment info maps to its connection status`() {
    val infos = listOf(
      ConnectedPaymentInfo.Active,
      ConnectedPaymentInfo.Pending,
      ConnectedPaymentInfo.NeedsPayinSetup(LocalDate(2026, 12, 3)),
      ConnectedPaymentInfo.NeedsPayinSetup(null),
      ConnectedPaymentInfo.NeedsPayoutSetup,
      ConnectedPaymentInfo.Unknown,
    )

    val statuses = infos.map {
      (content(connectedPaymentInfo = it).toScreenState() as PaymentOverviewScreenState.Content).connectionStatus
    }

    assertThat(statuses).containsExactly(
      PaymentConnectionStatus.Active,
      PaymentConnectionStatus.Pending,
      PaymentConnectionStatus.NeedsPayinSetup("2026-12-03"),
      PaymentConnectionStatus.NeedsPayinSetup(null),
      PaymentConnectionStatus.NeedsPayoutSetup,
      PaymentConnectionStatus.Unknown,
    )
  }

  @Test
  fun `each primary payin method maps to its method without its descriptor`() {
    val methods = listOf(
      PrimaryPayinMethod(PayinMethodId.Trustly, "Swedbank **** 4124"),
      PrimaryPayinMethod(PayinMethodId.Swish, "070-123 45 67"),
      PrimaryPayinMethod(PayinMethodId.Invoice, "Email"),
      null,
    )

    val mapped = methods.map {
      (content(primaryPayinMethod = it).toScreenState() as PaymentOverviewScreenState.Content).primaryPayinMethod
    }

    assertThat(mapped).containsExactly(
      PaymentPrimaryPayinMethod(PaymentChargeMethod.TRUSTLY, isPending = false),
      PaymentPrimaryPayinMethod(PaymentChargeMethod.SWISH, isPending = false),
      PaymentPrimaryPayinMethod(PaymentChargeMethod.INVOICE, isPending = false),
      null,
    )
  }

  @Test
  fun `each member type maps to its own entry`() {
    val mapped = MemberType.entries.map {
      (content(memberType = it).toScreenState() as PaymentOverviewScreenState.Content).memberType
    }

    assertThat(mapped).containsExactly(
      PaymentMemberType.QASA_ONLY,
      PaymentMemberType.STANDARD,
      PaymentMemberType.STANDARD_TO_QASA,
    )
  }

  @Test
  fun `isRetrying does not change the mapped state`() {
    assertThat(content(isRetrying = true).toScreenState()).isEqualTo(content(isRetrying = false).toScreenState())
  }

  @Test
  fun `handle calls the closure of each state`() {
    val states = listOf(
      PaymentOverviewScreenState.Loading,
      PaymentOverviewScreenState.Failed,
      content(memberType = MemberType.QASA_ONLY_MEMBER).toScreenState(),
    )

    val handled = states.map { state ->
      val calls = mutableListOf<String>()
      state.handle(
        onLoading = { calls += "loading" },
        onFailed = { calls += "failed" },
        onContent = { calls += "content:${it.memberType}" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("loading"), listOf("failed"), listOf("content:QASA_ONLY"))
  }

  @Test
  fun `handle calls the closure of each upcoming charge notice`() {
    val notices = listOf(
      PaymentUpcomingChargeNotice.NoNotice,
      PaymentUpcomingChargeNotice.InProgress,
      PaymentUpcomingChargeNotice.ChargeFailed("2026-09-01", "2026-09-30", null),
    )

    val handled = notices.map { notice ->
      val calls = mutableListOf<String>()
      notice.handle(
        onNoNotice = { calls += "none" },
        onInProgress = { calls += "in progress" },
        onChargeFailed = { calls += "failed:${it.failedPeriodFromDate}" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("none"), listOf("in progress"), listOf("failed:2026-09-01"))
  }

  @Test
  fun `handle calls the closure of each connection status`() {
    val statuses = listOf(
      PaymentConnectionStatus.Active,
      PaymentConnectionStatus.Pending,
      PaymentConnectionStatus.NeedsPayinSetup("2026-12-03"),
      PaymentConnectionStatus.NeedsPayoutSetup,
      PaymentConnectionStatus.Unknown,
    )

    val handled = statuses.map { status ->
      val calls = mutableListOf<String>()
      status.handle(
        onActive = { calls += "active" },
        onPending = { calls += "pending" },
        onNeedsPayinSetup = { calls += "payin:${it.dueDateToConnect}" },
        onNeedsPayoutSetup = { calls += "payout" },
        onUnknown = { calls += "unknown" },
      )
      calls
    }

    assertThat(handled).containsExactly(
      listOf("active"),
      listOf("pending"),
      listOf("payin:2026-12-03"),
      listOf("payout"),
      listOf("unknown"),
    )
  }

  @Test
  fun `handle calls the closure of each member type`() {
    val handled = PaymentMemberType.entries.map { type ->
      val calls = mutableListOf<String>()
      type.handle(
        onQasaOnly = { calls += "qasa only" },
        onStandard = { calls += "standard" },
        onStandardToQasa = { calls += "standard to qasa" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("qasa only"), listOf("standard"), listOf("standard to qasa"))
  }
}

private fun content(
  isRetrying: Boolean = false,
  upcomingPayment: UpcomingPayment = UpcomingPayment.NoUpcomingPayment,
  upcomingPaymentInfo: UpcomingPaymentInfo = UpcomingPaymentInfo.NoInfo,
  ongoingCharges: List<OngoingCharge> = emptyList(),
  connectedPaymentInfo: ConnectedPaymentInfo = ConnectedPaymentInfo.Active,
  primaryPayinMethod: PrimaryPayinMethod? = null,
  memberType: MemberType = MemberType.STANDARD_MEMBER,
  showRetryChargeNotice: Boolean = false,
  anyPayinMethodIsPending: Boolean = false,
) = PaymentsUiState.Content(
  isRetrying = isRetrying,
  upcomingPayment = upcomingPayment,
  upcomingPaymentInfo = upcomingPaymentInfo,
  ongoingCharges = ongoingCharges,
  connectedPaymentInfo = connectedPaymentInfo,
  primaryPayinMethod = primaryPayinMethod,
  memberType = memberType,
  showRetryChargeNotice = showRetryChargeNotice,
  anyPayinMethodIsPending = anyPayinMethodIsPending,
)
