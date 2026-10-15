package com.hedvig.android.feature.payments.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge.ChargeBreakdown.Period
import kotlinx.datetime.LocalDate
import org.junit.Test

class MemberChargeTest {
  private val sek = UiMoney(100.0, UiCurrencyCode.SEK)

  @Test
  fun `charge methods are recognised by their provider prefix, ignoring case`() {
    assertThat("KIVRA".toChargeMethod()).isEqualTo(MemberPaymentChargeMethod.INVOICE)
    assertThat("invoice_mail".toChargeMethod()).isEqualTo(MemberPaymentChargeMethod.INVOICE)
    assertThat("Trustly".toChargeMethod()).isEqualTo(MemberPaymentChargeMethod.TRUSTLY)
    assertThat("swish_payin".toChargeMethod()).isEqualTo(MemberPaymentChargeMethod.SWISH)
    assertThat("nordea".toChargeMethod()).isEqualTo(MemberPaymentChargeMethod.UNKNOWN)
    assertThat(null.toChargeMethod()).isEqualTo(MemberPaymentChargeMethod.UNKNOWN)
  }

  @Test
  fun `the last day of a month is recognised, including leap years`() {
    assertThat(LocalDate(2026, 1, 31).isLastDayOfMonth()).isTrue()
    assertThat(LocalDate(2026, 4, 30).isLastDayOfMonth()).isTrue()
    assertThat(LocalDate(2026, 2, 28).isLastDayOfMonth()).isTrue()
    assertThat(LocalDate(2028, 2, 28).isLastDayOfMonth()).isFalse()
    assertThat(LocalDate(2028, 2, 29).isLastDayOfMonth()).isTrue()
    assertThat(LocalDate(2026, 12, 30).isLastDayOfMonth()).isFalse()
  }

  @Test
  fun `a period covering a whole calendar month is a full period`() {
    val period = Period(sek, LocalDate(2026, 2, 1), LocalDate(2026, 2, 28), isPreviouslyFailedCharge = false)
    assertThat(period.description).isEqualTo(Period.Description.FullPeriod)
  }

  @Test
  fun `a partial period counts the days between its dates`() {
    val startsMidMonth = Period(sek, LocalDate(2026, 3, 15), LocalDate(2026, 3, 31), isPreviouslyFailedCharge = false)
    assertThat(startsMidMonth.description).isEqualTo(Period.Description.BetweenDays(16))

    val endsMidMonth = Period(sek, LocalDate(2026, 3, 1), LocalDate(2026, 3, 10), isPreviouslyFailedCharge = false)
    assertThat(endsMidMonth.description).isEqualTo(Period.Description.BetweenDays(9))
  }

  @Test
  fun `adjustments are only shown when they are above zero`() {
    val zero = UiMoney(0.0, UiCurrencyCode.SEK)
    val negative = UiMoney(-50.0, UiCurrencyCode.SEK)

    assertThat(charge(carried = sek, settlement = sek).carriedAdjustmentIfAboveZero()).isEqualTo(sek)
    assertThat(charge(carried = sek, settlement = sek).settlementAdjustmentIfAboveZero()).isEqualTo(sek)
    assertThat(charge(carried = zero, settlement = zero).carriedAdjustmentIfAboveZero()).isNull()
    assertThat(charge(carried = zero, settlement = zero).settlementAdjustmentIfAboveZero()).isNull()
    assertThat(charge(carried = negative, settlement = negative).carriedAdjustmentIfAboveZero()).isNull()
    assertThat(charge(carried = negative, settlement = negative).settlementAdjustmentIfAboveZero()).isNull()
    assertThat(charge(carried = null, settlement = null).carriedAdjustmentIfAboveZero()).isNull()
    assertThat(charge(carried = null, settlement = null).settlementAdjustmentIfAboveZero()).isNull()
  }

  private fun charge(carried: UiMoney?, settlement: UiMoney?) = MemberCharge(
    grossAmount = sek,
    netAmount = sek,
    id = "id",
    status = MemberCharge.MemberChargeStatus.UPCOMING,
    dueDate = LocalDate(2026, 11, 27),
    failedCharge = null,
    chargeBreakdowns = emptyList(),
    referralDiscount = null,
    carriedAdjustment = carried,
    settlementAdjustment = settlement,
    chargeMethod = MemberPaymentChargeMethod.TRUSTLY,
  )
}
