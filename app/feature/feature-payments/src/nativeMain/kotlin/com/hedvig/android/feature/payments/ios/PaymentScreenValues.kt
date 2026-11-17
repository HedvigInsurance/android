package com.hedvig.android.feature.payments.ios

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import kotlinx.datetime.LocalDate

/** An amount as the backend sends it, unrounded. [currencyCode] is an ISO 4217 code such as "SEK". */
data class PaymentMoney(val amount: Double, val currencyCode: String)

enum class PaymentChargeStatus {
  UPCOMING,
  SUCCESS,
  PENDING,
  FAILED,
  UNKNOWN,
  ;

  /** Calls the closure for this entry, so a Swift call site stops compiling when an entry is added. */
  fun handle(
    onUpcoming: () -> Unit,
    onSuccess: () -> Unit,
    onPending: () -> Unit,
    onFailed: () -> Unit,
    onUnknown: () -> Unit,
  ) {
    when (this) {
      UPCOMING -> onUpcoming()
      SUCCESS -> onSuccess()
      PENDING -> onPending()
      FAILED -> onFailed()
      UNKNOWN -> onUnknown()
    }
  }
}

internal fun UiMoney.toPaymentMoney(): PaymentMoney = PaymentMoney(amount, currencyCode.name)

/** The calendar day as `yyyy-MM-dd`. */
internal fun LocalDate.toIsoDateString(): String = LocalDate.Formats.ISO.format(this)

internal fun MemberCharge.MemberChargeStatus.toPaymentChargeStatus(): PaymentChargeStatus = when (this) {
  MemberCharge.MemberChargeStatus.UPCOMING -> PaymentChargeStatus.UPCOMING
  MemberCharge.MemberChargeStatus.SUCCESS -> PaymentChargeStatus.SUCCESS
  MemberCharge.MemberChargeStatus.PENDING -> PaymentChargeStatus.PENDING
  MemberCharge.MemberChargeStatus.FAILED -> PaymentChargeStatus.FAILED
  MemberCharge.MemberChargeStatus.UNKNOWN -> PaymentChargeStatus.UNKNOWN
}

/** A charge without an id is null on the Swift side; the data layer stores some of them as "". */
internal fun String?.toChargeIdOrNull(): String? = this?.takeIf { it.isNotEmpty() }
