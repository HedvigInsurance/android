package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.Discount.ExpiredState
import kotlin.String
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
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
    val discounts: List<Discount>,
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
          MemberCharge.ChargeBreakdown.Period.Description.FullPeriod
        }

        else -> {
          MemberCharge.ChargeBreakdown.Period.Description.BetweenDays(fromDate.daysUntil(toDate))
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
      octopus.type.MemberChargeStatus.UPCOMING -> MemberCharge.MemberChargeStatus.UPCOMING
      octopus.type.MemberChargeStatus.SUCCESS -> MemberCharge.MemberChargeStatus.SUCCESS
      octopus.type.MemberChargeStatus.PENDING -> MemberCharge.MemberChargeStatus.PENDING
      octopus.type.MemberChargeStatus.FAILED -> MemberCharge.MemberChargeStatus.FAILED
      octopus.type.MemberChargeStatus.UNKNOWN__ -> MemberCharge.MemberChargeStatus.UNKNOWN
    },
    dueDate = date,
  )
}

internal fun MemberChargeFragment.toMemberCharge(
  redeemedCampaigns: List<PaymentHistoryWithDetailsQuery.Data.CurrentMember.RedeemedCampaign>,
  referralInformation: PaymentHistoryWithDetailsQuery.Data.CurrentMember.ReferralInformation,
  clock: Clock,
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
      discounts = chargeBreakdown.discounts?.map { discount ->
        val relatedRedeemedCampaign = redeemedCampaigns.firstOrNull { it.code == discount.code }
        Discount(
          code = discount.code,
          displayName = redeemedCampaigns.firstOrNull {
            it.code == discount.code
          }?.onlyApplicableToContracts?.firstOrNull()?.exposureDisplayName,
          description = relatedRedeemedCampaign?.description,
          expiredState = Discount.ExpiredState.from(relatedRedeemedCampaign?.expiresAt, clock),
          amount = UiMoney(
            discount.discount.amount,
            UiCurrencyCode.fromCurrencyCode(discount.discount.currencyCode),
          ),
          isReferral = false,
        )
      } ?: listOf(),
    )
  },
  settlementAdjustment = settlementAdjustment?.let(UiMoney::fromMoneyFragment),
  carriedAdjustment = carriedAdjustment?.let(UiMoney::fromMoneyFragment),
  referralDiscount = this.referralDiscount?.let {
    Discount(
      code = referralInformation.code,
      displayName = null,
      expiredState = ExpiredState.NotExpired,
      description = null,
      amount = UiMoney(
        it.amount.unaryMinus(),
        UiCurrencyCode.fromCurrencyCode(it.currencyCode),
      ),
      isReferral = true,
    )
  },
)

internal fun MemberChargeFragment.toFailedCharge(): MemberCharge.FailedCharge? {
  val previousChargesPeriods = chargeBreakdown
    .flatMap { it.periods }
    .filter { it.isPreviouslyFailedCharge }

  val from = previousChargesPeriods.minOfOrNull { it.fromDate }
  val to = previousChargesPeriods.maxOfOrNull { it.toDate }

  return if (from != null && to != null) {
    MemberCharge.FailedCharge(
      from,
      to,
    )
  } else {
    null
  }
}

private fun Discount.ExpiredState.Companion.from(expirationDate: LocalDate?, clock: Clock): Discount.ExpiredState {
  if (expirationDate == null) {
    return Discount.ExpiredState.NotExpired
  }
  val today = clock.todayIn(TimeZone.currentSystemDefault())
  return if (expirationDate < today) {
    Discount.ExpiredState.AlreadyExpired(expirationDate)
  } else {
    Discount.ExpiredState.ExpiringInTheFuture(expirationDate)
  }
}

fun LocalDate.isLastDayOfMonth(): Boolean {
  return toJavaLocalDate().lengthOfMonth() == dayOfMonth
}
