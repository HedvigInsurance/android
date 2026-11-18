package com.hedvig.android.feature.payments.ios

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.InvoiceDelivery
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState
import com.hedvig.android.molecule.public.NavigationRequest
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class PaymentManualChargeScreenStateTest {
  @Test
  fun `Loading maps to Loading`() {
    assertThat(ManualChargeUiState.Loading.toScreenState()).isEqualTo(PaymentManualChargeScreenState.Loading)
  }

  @Test
  fun `NoLongerChargeable maps to NoLongerChargeable`() {
    assertThat(ManualChargeUiState.NoLongerChargeable.toScreenState())
      .isEqualTo(PaymentManualChargeScreenState.NoLongerChargeable)
  }

  @Test
  fun `a failure keeps the backend's message and nothing else`() {
    val withMessage = ManualChargeUiState.Failure(ErrorMessage("Changes have been made", IllegalStateException("x")))
    val withoutMessage = ManualChargeUiState.Failure(ErrorMessage())

    assertThat(listOf(withMessage, withoutMessage).map { it.toScreenState() }).containsExactly(
      PaymentManualChargeScreenState.Failed("Changes have been made"),
      PaymentManualChargeScreenState.Failed(null),
    )
  }

  @Test
  fun `a success maps every field`() {
    val state = ManualChargeUiState.Success(info(), payButtonLoading = false)

    assertThat(state.toScreenState()).isEqualTo(
      PaymentManualChargeScreenState.Content(
        chargeId = "missed-1",
        missedDueDate = "2026-09-27",
        amountDue = PaymentMoney(399.5, "SEK"),
        payinMethods = listOf(
          PaymentConnectedPayinAccount(
            PaymentPayinAccount.Invoice(PaymentInvoiceDelivery.KIVRA, "member@example.test"),
            isPending = true,
            isDefault = false,
          ),
          PaymentConnectedPayinAccount(
            PaymentPayinAccount.Swish("0701234567"),
            isPending = false,
            isDefault = false,
          ),
          PaymentConnectedPayinAccount(
            PaymentPayinAccount.Trustly("8327", "91234124", "Swedbank"),
            isPending = false,
            isDefault = true,
          ),
        ),
        primaryPayinAccount = PaymentPayinAccount.Trustly("8327", "91234124", "Swedbank"),
        showsCancellationWarning = true,
        isCharging = false,
      ),
    )
  }

  @Test
  fun `a success with the pay button loading is charging`() {
    val mapped = ManualChargeUiState.Success(info(), payButtonLoading = true).toScreenState()

    assertThat((mapped as PaymentManualChargeScreenState.Content).isCharging).isEqualTo(true)
  }

  @Test
  fun `a success without a charge id or primary method maps them to null`() {
    val mapped = ManualChargeUiState.Success(info(chargeId = null, primary = null)).toScreenState()
    val emptyId = ManualChargeUiState.Success(info(chargeId = "")).toScreenState()

    mapped as PaymentManualChargeScreenState.Content
    assertThat(listOf(mapped.chargeId, mapped.primaryPayinAccount)).containsExactly(null, null)
    assertThat((emptyId as PaymentManualChargeScreenState.Content).chargeId).isEqualTo(null)
  }

  @Test
  fun `Charged carries the cancellation warning and the method charged`() {
    val primaries = listOf(trustly, swish, invoice, null)

    val charged = primaries.mapIndexed { index, primary ->
      ManualChargeUiState.Charged(
        NavigationRequest.of(info(primary = primary, showCancellationWarning = index % 2 == 0)),
      ).toScreenState()
    }

    assertThat(charged).containsExactly(
      PaymentManualChargeScreenState.Charged(showsCancellationWarning = true, PaymentChargeMethod.TRUSTLY),
      PaymentManualChargeScreenState.Charged(showsCancellationWarning = false, PaymentChargeMethod.SWISH),
      PaymentManualChargeScreenState.Charged(showsCancellationWarning = true, PaymentChargeMethod.INVOICE),
      PaymentManualChargeScreenState.Charged(showsCancellationWarning = false, primaryPayinMethod = null),
    )
  }

  @Test
  fun `the content's text leaves out the member's account details`() {
    val text = ManualChargeUiState.Success(info()).toScreenState().toString()

    assertThat(text).doesNotContain("91234124")
    assertThat(text).doesNotContain("8327")
    assertThat(text).doesNotContain("0701234567")
    assertThat(text).doesNotContain("member@example.test")
  }

  @Test
  fun `handle calls the closure of each state`() {
    val states = listOf(
      PaymentManualChargeScreenState.Loading,
      PaymentManualChargeScreenState.NoLongerChargeable,
      PaymentManualChargeScreenState.Failed("message"),
      ManualChargeUiState.Success(info()).toScreenState(),
      PaymentManualChargeScreenState.Charged(showsCancellationWarning = false, PaymentChargeMethod.SWISH),
    )

    val handled = states.map { state ->
      val calls = mutableListOf<String>()
      state.handle(
        onLoading = { calls += "loading" },
        onNoLongerChargeable = { calls += "no longer chargeable" },
        onFailed = { calls += "failed:${it.userErrorMessage}" },
        onContent = { calls += "content:${it.chargeId}" },
        onCharged = { calls += "charged:${it.primaryPayinMethod}" },
      )
      calls
    }

    assertThat(handled).containsExactly(
      listOf("loading"),
      listOf("no longer chargeable"),
      listOf("failed:message"),
      listOf("content:missed-1"),
      listOf("charged:SWISH"),
    )
  }
}

private val trustly = PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = false, isDefault = true)
private val swish = PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = false)
private val invoice = PayinAccount.Invoice(
  InvoiceDelivery.Kivra,
  "member@example.test",
  isPending = true,
  isDefault = false,
)

private fun info(
  chargeId: String? = "missed-1",
  primary: PayinAccount? = trustly,
  showCancellationWarning: Boolean = true,
) = ManualChargeInfo(
  chargeId = chargeId,
  missedDueDate = LocalDate(2026, 9, 27),
  amountDue = UiMoney(399.5, UiCurrencyCode.SEK),
  currentMethods = listOf(invoice, swish, trustly),
  primaryPayinMethod = primary,
  showCancellationWarning = showCancellationWarning,
)
