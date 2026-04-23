package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.Discount.DiscountStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDate
import kotlinx.serialization.Serializable
import octopus.PaymentHistoryWithDetailsQuery
import octopus.ShortPaymentHistoryQuery
import octopus.fragment.MemberChargeFragment
import octopus.type.MemberChargeStatus

@Serializable
internal data class MemberCharge(
  val grossAmount: UiMoney,
  val netAmount: UiMoney,
  val id: String?,
  val status: MemberChargeStatus,
  val dueDate: LocalDate,
  val failedCharge: FailedCharge?,
  val chargeBreakdowns: List<ChargeBreakdown>,
  val referralDiscount: Discount?,
  private val carriedAdjustment: UiMoney?,
  private val settlementAdjustment: UiMoney?,
  val chargeMethod: MemberPaymentChargeMethod,
) {
  fun carriedAdjustmentIfAboveZero(): UiMoney? = if (carriedAdjustment != null && carriedAdjustment.amount > 0) {
    carriedAdjustment
  } else {
    null
  }

  fun settlementAdjustmentIfAboveZero(): UiMoney? =
    if (settlementAdjustment != null && settlementAdjustment.amount > 0) {
      settlementAdjustment
    } else {
      null
    }

  @Serializable
  data class FailedCharge(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val sum: UiMoney,
  )

  internal enum class MemberChargeStatus {
    UPCOMING,
    SUCCESS,
    PENDING,
    FAILED,
    UNKNOWN,
  }

  @Serializable
  data class ChargeBreakdown(
    val contractDisplayName: String,
    val contractDetails: String,
    val grossAmount: UiMoney,
    val netAmount: UiMoney,
    val periods: List<Period>,
    val priceBreakdown: List<Pair<String, UiMoney>>,
  ) {
    @Serializable
    data class Period(
      val amount: UiMoney,
      val fromDate: LocalDate,
      val toDate: LocalDate,
      val isPreviouslyFailedCharge: Boolean,
    ) {
      val description: Description? = when {
        fromDate.dayOfMonth == 1 && toDate.isLastDayOfMonth() -> {
          Description.FullPeriod
        }

        else -> {
          Description.BetweenDays(fromDate.daysUntil(toDate))
        }
      }

      sealed interface Description {
        data object FullPeriod : Description

        data class BetweenDays(val daysBetween: Int) : Description
      }
    }
  }
}

@Serializable
internal data class PaymentHistoryItem(
  val netAmount: UiMoney,
  val id: String,
  val status: MemberCharge.MemberChargeStatus,
  val dueDate: LocalDate,
)

internal fun ShortPaymentHistoryQuery.Data.CurrentMember.PastCharge.toPaymentHistoryItem(): PaymentHistoryItem {
  return PaymentHistoryItem(
    id = id ?: "",
    netAmount = UiMoney.fromMoneyFragment(net),
    status = when (status) {
      MemberChargeStatus.UPCOMING -> MemberCharge.MemberChargeStatus.UPCOMING
      MemberChargeStatus.SUCCESS -> MemberCharge.MemberChargeStatus.SUCCESS
      MemberChargeStatus.PENDING -> MemberCharge.MemberChargeStatus.PENDING
      MemberChargeStatus.FAILED -> MemberCharge.MemberChargeStatus.FAILED
      MemberChargeStatus.UNKNOWN__ -> MemberCharge.MemberChargeStatus.UNKNOWN
    },
    dueDate = date,
  )
}

internal fun MemberChargeFragment.toMemberCharge(
  referralInformation: PaymentHistoryWithDetailsQuery.Data.CurrentMember.ReferralInformation,
) = MemberCharge(
  id = id,
  grossAmount = UiMoney.fromMoneyFragment(gross),
  netAmount = UiMoney.fromMoneyFragment(net),
  status = when (status) {
    MemberChargeStatus.UPCOMING -> MemberCharge.MemberChargeStatus.UPCOMING
    MemberChargeStatus.SUCCESS -> MemberCharge.MemberChargeStatus.SUCCESS
    MemberChargeStatus.PENDING -> MemberCharge.MemberChargeStatus.PENDING
    MemberChargeStatus.FAILED -> MemberCharge.MemberChargeStatus.FAILED
    MemberChargeStatus.UNKNOWN__ -> MemberCharge.MemberChargeStatus.UNKNOWN
  },
  dueDate = date,
  failedCharge = toFailedCharge(),
  chargeBreakdowns = chargeBreakdown.map { chargeBreakdown ->
    MemberCharge.ChargeBreakdown(
      contractDisplayName = chargeBreakdown.displayTitle,
      contractDetails = chargeBreakdown.displaySubtitle ?: "",
      grossAmount = UiMoney.fromMoneyFragment(chargeBreakdown.gross),
      netAmount = UiMoney.fromMoneyFragment(chargeBreakdown.net),
      periods = chargeBreakdown.periods.map {
        MemberCharge.ChargeBreakdown.Period(
          amount = UiMoney.fromMoneyFragment(it.amount),
          fromDate = it.fromDate,
          toDate = it.toDate,
          isPreviouslyFailedCharge = it.isPreviouslyFailedCharge,
        )
      },
      priceBreakdown = chargeBreakdown.insurancePriceBreakdown.map {
        it.displayTitle to UiMoney.fromMoneyFragment(it.amount)
      },
    )
  },
  settlementAdjustment = settlementAdjustment?.let(UiMoney::fromMoneyFragment),
  carriedAdjustment = carriedAdjustment?.let(UiMoney::fromMoneyFragment),
  referralDiscount = this.referralDiscount?.let {
    Discount(
      code = referralInformation.code,
      // Expired state is not applicable in this context
      status = DiscountStatus.ACTIVE,
      description = null,
      amount = UiMoney(
        it.amount.unaryMinus(),
        UiCurrencyCode.fromCurrencyCode(it.currencyCode),
      ),
      isReferral = true,
      statusDescription = null,
    )
  },
  chargeMethod = paymentProvider.toChargeMethod(),
)

internal fun String?.toChargeMethod(): MemberPaymentChargeMethod {
  return when {
    this?.startsWith("kivra", ignoreCase = true) == true -> MemberPaymentChargeMethod.KIVRA
    this?.startsWith("trustly", ignoreCase = true) == true -> MemberPaymentChargeMethod.TRUSTLY
    else -> MemberPaymentChargeMethod.UNKNOWN
  }
}

internal fun MemberChargeFragment.toFailedCharge(): MemberCharge.FailedCharge? {
  val previousChargesPeriods = chargeBreakdown
    .flatMap { it.periods }
    .filter { it.isPreviouslyFailedCharge }

  val from = previousChargesPeriods.minOfOrNull { it.fromDate }
  val to = previousChargesPeriods.maxOfOrNull { it.toDate }
  val sum = if (previousChargesPeriods.isNotEmpty()) {
    UiMoney(
      previousChargesPeriods.sumOf { it.amount.amount },
      UiCurrencyCode.fromCurrencyCode(previousChargesPeriods.first().amount.currencyCode),
    )
  } else {
    UiMoney(0.0, UiCurrencyCode.SEK)
  }

  return if (from != null && to != null) {
    MemberCharge.FailedCharge(
      from,
      to,
      sum,
    )
  } else {
    null
  }
}

fun LocalDate.isLastDayOfMonth(): Boolean {
  return toJavaLocalDate().lengthOfMonth() == dayOfMonth
}
