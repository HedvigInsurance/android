package com.hedvig.android.feature.payments.ui.payments

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberChargeShortInfo
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPayment
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class PaymentsPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val dueDate = LocalDate(2026, 11, 27)
  private val amount = UiMoney(399.0, UiCurrencyCode.SEK)

  @Test
  fun `a loaded overview shows the upcoming payment`() = runTest {
    val useCase = FakeGetUpcomingPaymentUseCase()
    PaymentsPresenter(useCase).test(PaymentsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      useCase.responses.send(overview(charge = charge()).right())
      assertThat(awaitItem()).isInstanceOf<PaymentsUiState.Content>().prop(PaymentsUiState.Content::upcomingPayment)
        .isEqualTo(UpcomingPayment.Content(netAmount = amount, dueDate = dueDate, id = "chargeId"))
    }
  }

  @Test
  fun `no future charge shows no upcoming payment and no info`() = runTest {
    val useCase = FakeGetUpcomingPaymentUseCase()
    PaymentsPresenter(useCase).test(PaymentsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      useCase.responses.send(overview(charge = null).right())
      val content = awaitItem() as PaymentsUiState.Content
      assertThat(content.upcomingPayment).isEqualTo(UpcomingPayment.NoUpcomingPayment)
      assertThat(content.upcomingPaymentInfo).isEqualTo(UpcomingPaymentInfo.NoInfo)
    }
  }

  @Test
  fun `a pending charge is shown as in progress even when it carries a failed charge`() = runTest {
    val useCase = FakeGetUpcomingPaymentUseCase()
    PaymentsPresenter(useCase).test(PaymentsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      val pendingCharge = charge(status = MemberCharge.MemberChargeStatus.PENDING, failedCharge = failedCharge())
      useCase.responses.send(overview(charge = pendingCharge).right())
      assertThat(awaitItem()).isInstanceOf<PaymentsUiState.Content>()
        .prop(PaymentsUiState.Content::upcomingPaymentInfo)
        .isEqualTo(UpcomingPaymentInfo.InProgress)
    }
  }

  @Test
  fun `a charge carrying a failed one shows the failed period and the manual charge offer`() = runTest {
    val useCase = FakeGetUpcomingPaymentUseCase()
    val manualCharge = ManualChargeToPrompt(UiMoney(120.0, UiCurrencyCode.SEK))
    PaymentsPresenter(useCase).test(PaymentsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      useCase.responses.send(
        overview(charge = charge(failedCharge = failedCharge()), isManualChargeAllowed = manualCharge).right(),
      )
      assertThat(awaitItem()).isInstanceOf<PaymentsUiState.Content>()
        .prop(PaymentsUiState.Content::upcomingPaymentInfo)
        .isEqualTo(
          UpcomingPaymentInfo.PaymentFailed(
            failedPaymentStartDate = LocalDate(2026, 9, 1),
            failedPaymentEndDate = LocalDate(2026, 9, 30),
            isManualChargeAllowed = manualCharge,
          ),
        )
    }
  }

  @Test
  fun `each payment connection maps to its connected payment info`() = runTest {
    val terminationDate = LocalDate(2026, 12, 31)
    val expectations = listOf(
      PaymentConnection.Active to ConnectedPaymentInfo.Active,
      PaymentConnection.Pending to ConnectedPaymentInfo.Pending,
      PaymentConnection.NeedsPayinSetup(terminationDate) to ConnectedPaymentInfo.NeedsPayinSetup(terminationDate),
      PaymentConnection.NeedsPayoutSetup to ConnectedPaymentInfo.NeedsPayoutSetup,
      PaymentConnection.Unknown to ConnectedPaymentInfo.Unknown,
    )
    for ((connection, expected) in expectations) {
      val useCase = FakeGetUpcomingPaymentUseCase()
      PaymentsPresenter(useCase).test(PaymentsUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
        useCase.responses.send(overview(charge = null, paymentConnection = connection).right())
        assertThat(awaitItem()).isInstanceOf<PaymentsUiState.Content>()
          .prop(PaymentsUiState.Content::connectedPaymentInfo)
          .isEqualTo(expected)
      }
    }
  }

  @Test
  fun `a failed load shows an error, and retrying loads again`() = runTest {
    val useCase = FakeGetUpcomingPaymentUseCase()
    PaymentsPresenter(useCase).test(PaymentsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      useCase.responses.send(ErrorMessage().left())
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Error)
      sendEvent(PaymentsEvent.Retry)
      assertThat(awaitItem()).isEqualTo(PaymentsUiState.Loading)
      useCase.responses.send(overview(charge = charge()).right())
      assertThat(awaitItem()).isInstanceOf<PaymentsUiState.Content>()
      assertThat(useCase.calls).isEqualTo(2)
    }
  }

  private fun failedCharge() = MemberCharge.FailedCharge(
    fromDate = LocalDate(2026, 9, 1),
    toDate = LocalDate(2026, 9, 30),
    sum = UiMoney(120.0, UiCurrencyCode.SEK),
  )

  private fun charge(
    status: MemberCharge.MemberChargeStatus = MemberCharge.MemberChargeStatus.UPCOMING,
    failedCharge: MemberCharge.FailedCharge? = null,
  ) = MemberChargeShortInfo(
    netAmount = amount,
    dueDate = dueDate,
    id = "chargeId",
    status = status,
    failedCharge = failedCharge,
  )

  private fun overview(
    charge: MemberChargeShortInfo?,
    paymentConnection: PaymentConnection = PaymentConnection.Active,
    isManualChargeAllowed: ManualChargeToPrompt? = null,
  ) = PaymentOverview(
    memberChargeShortInfo = charge,
    ongoingCharges = emptyList(),
    paymentConnection = paymentConnection,
    primaryPayinMethod = null,
    isManualChargeAllowed = isManualChargeAllowed,
    memberType = MemberType.STANDARD_MEMBER,
    showRetryChargeNotice = false,
    anyPayinMethodIsPending = false,
  )
}

private class FakeGetUpcomingPaymentUseCase : GetUpcomingPaymentUseCase {
  val responses = Channel<Either<ErrorMessage, PaymentOverview>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> {
    calls++
    return responses.receive()
  }
}
