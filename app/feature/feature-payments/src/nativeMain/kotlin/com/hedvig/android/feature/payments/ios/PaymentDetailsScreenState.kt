package com.hedvig.android.feature.payments.ios

sealed class PaymentDetailsScreenState {
  data object Loading : PaymentDetailsScreenState()

  data object Failed : PaymentDetailsScreenState()

  /**
   * One charge. Dates are `yyyy-MM-dd`.
   *
   * [carriedAdjustment] and [settlementAdjustment] are null unless above zero. [payinAccount] is the member's default
   * account, present only for an upcoming or ongoing charge made with that account's method.
   *
   * [nextChargeId] and [nextChargeDueDate] describe the charge after this one, or the upcoming charge when this is the
   * last past charge: the charge a failed amount is added to. [nextChargeId] is null for a next charge without an id.
   */
  data class Content(
    val id: String?,
    val status: PaymentChargeStatus,
    val dueDate: String,
    val grossAmount: PaymentMoney,
    val netAmount: PaymentMoney,
    val chargeBreakdowns: List<PaymentChargeBreakdown>,
    val carriedAdjustment: PaymentMoney?,
    val settlementAdjustment: PaymentMoney?,
    val referralDiscount: PaymentReferralDiscount?,
    val chargeMethod: PaymentChargeMethod,
    val payinAccount: PaymentPayinAccount?,
    val nextChargeId: String?,
    val nextChargeDueDate: String?,
  ) : PaymentDetailsScreenState()

  /** Calls the closure for this state's case, so a Swift call site stops compiling when a case is added. */
  fun handle(onLoading: () -> Unit, onFailed: () -> Unit, onContent: (Content) -> Unit) {
    when (this) {
      Loading -> onLoading()
      Failed -> onFailed()
      is Content -> onContent(this)
    }
  }
}

/** The part of a charge that pays for one contract. The display texts come from the backend. */
data class PaymentChargeBreakdown(
  val contractDisplayName: String,
  val contractDetails: String,
  val grossAmount: PaymentMoney,
  val netAmount: PaymentMoney,
  val periods: List<PaymentChargePeriod>,
  val priceBreakdown: List<PaymentPriceBreakdownItem>,
)

/** [fromDate] and [toDate] are `yyyy-MM-dd`. */
data class PaymentChargePeriod(
  val amount: PaymentMoney,
  val fromDate: String,
  val toDate: String,
  val isPreviouslyFailedCharge: Boolean,
  val periodDescription: PaymentPeriodDescription?,
)

sealed class PaymentPeriodDescription {
  /** The period covers a whole calendar month. */
  data object FullPeriod : PaymentPeriodDescription()

  data class BetweenDays(val daysBetween: Int) : PaymentPeriodDescription()

  /** Calls the closure for this case, so a Swift call site stops compiling when a case is added. */
  fun handle(onFullPeriod: () -> Unit, onBetweenDays: (BetweenDays) -> Unit) {
    when (this) {
      FullPeriod -> onFullPeriod()
      is BetweenDays -> onBetweenDays(this)
    }
  }
}

/** [displayTitle] comes from the backend. */
data class PaymentPriceBreakdownItem(val displayTitle: String, val amount: PaymentMoney)

/** [amount] is negative: it is taken off the charge. */
data class PaymentReferralDiscount(val code: String, val amount: PaymentMoney?)

enum class PaymentChargeMethod {
  TRUSTLY,
  SWISH,
  INVOICE,
  UNKNOWN,
  ;

  /** Calls the closure for this entry, so a Swift call site stops compiling when an entry is added. */
  fun handle(onTrustly: () -> Unit, onSwish: () -> Unit, onInvoice: () -> Unit, onUnknown: () -> Unit) {
    when (this) {
      TRUSTLY -> onTrustly()
      SWISH -> onSwish()
      INVOICE -> onInvoice()
      UNKNOWN -> onUnknown()
    }
  }
}

/**
 * The account a charge is taken from, unmasked as the backend sends it. Each case's `toString`, which Swift sees as
 * `description()`, leaves the member's details out, so a logged state does not carry them.
 */
sealed class PaymentPayinAccount {
  data class Trustly(
    val clearingNumber: String?,
    val accountNumber: String?,
    val bankName: String?,
  ) : PaymentPayinAccount() {
    override fun toString(): String = "Trustly(bankName=$bankName)"
  }

  data class Swish(val phoneNumber: String?) : PaymentPayinAccount() {
    override fun toString(): String = "Swish"
  }

  data class Invoice(val delivery: PaymentInvoiceDelivery?, val email: String?) : PaymentPayinAccount() {
    override fun toString(): String = "Invoice(delivery=$delivery)"
  }

  /** Calls the closure for this case, so a Swift call site stops compiling when a case is added. */
  fun handle(onTrustly: (Trustly) -> Unit, onSwish: (Swish) -> Unit, onInvoice: (Invoice) -> Unit) {
    when (this) {
      is Trustly -> onTrustly(this)
      is Swish -> onSwish(this)
      is Invoice -> onInvoice(this)
    }
  }
}

enum class PaymentInvoiceDelivery {
  KIVRA,
  MAIL,
  ;

  /** Calls the closure for this entry, so a Swift call site stops compiling when an entry is added. */
  fun handle(onKivra: () -> Unit, onMail: () -> Unit) {
    when (this) {
      KIVRA -> onKivra()
      MAIL -> onMail()
    }
  }
}
