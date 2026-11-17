package com.hedvig.android.feature.payments.ios

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class PaymentScreenValuesTest {
  @Test
  fun `money keeps its amount unrounded and its ISO currency code`() {
    assertThat(UiMoney(1234.56, UiCurrencyCode.SEK).toPaymentMoney()).isEqualTo(PaymentMoney(1234.56, "SEK"))
    assertThat(UiMoney(-20.0, UiCurrencyCode.NOK).toPaymentMoney()).isEqualTo(PaymentMoney(-20.0, "NOK"))
    assertThat(UiMoney(0.5, UiCurrencyCode.DKK).toPaymentMoney()).isEqualTo(PaymentMoney(0.5, "DKK"))
  }

  @Test
  fun `a date is an ISO yyyy-MM-dd string with padded month and day`() {
    assertThat(LocalDate(2026, 1, 5).toIsoDateString()).isEqualTo("2026-01-05")
    assertThat(LocalDate(2026, 12, 31).toIsoDateString()).isEqualTo("2026-12-31")
  }

  @Test
  fun `each charge status maps to its own entry`() {
    val mapped = MemberCharge.MemberChargeStatus.entries.map { it.toPaymentChargeStatus() }

    assertThat(mapped).containsExactly(
      PaymentChargeStatus.UPCOMING,
      PaymentChargeStatus.SUCCESS,
      PaymentChargeStatus.PENDING,
      PaymentChargeStatus.FAILED,
      PaymentChargeStatus.UNKNOWN,
    )
  }

  @Test
  fun `an empty charge id is null and any other id is kept`() {
    assertThat("".toChargeIdOrNull()).isNull()
    assertThat((null as String?).toChargeIdOrNull()).isNull()
    assertThat("charge-1".toChargeIdOrNull()).isEqualTo("charge-1")
    assertThat(" ".toChargeIdOrNull()).isEqualTo(" ")
  }

  @Test
  fun `handle calls the closure of each charge status`() {
    val handled = PaymentChargeStatus.entries.map { status ->
      val calls = mutableListOf<String>()
      status.handle(
        onUpcoming = { calls += "upcoming" },
        onSuccess = { calls += "success" },
        onPending = { calls += "pending" },
        onFailed = { calls += "failed" },
        onUnknown = { calls += "unknown" },
      )
      calls
    }

    assertThat(handled).containsExactly(
      listOf("upcoming"),
      listOf("success"),
      listOf("pending"),
      listOf("failed"),
      listOf("unknown"),
    )
  }
}
