package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import octopus.UpcomingPaymentQuery
import octopus.fragment.MemberChargeFragment
import octopus.type.CurrencyCode
import octopus.type.MemberChargeStatus
import octopus.type.MemberPaymentConnectionStatus
import octopus.type.RedeemedCampaignType

internal interface GetUpcomingPaymentUseCase {
  suspend fun invoke(): Either<ErrorMessage, PaymentOverview>
}

internal data class GetUpcomingPaymentUseCaseImpl(
  val apolloClient: ApolloClient,
  val clock: Clock,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> = either {
    val result = apolloClient.query(UpcomingPaymentQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    val redeemedCampaigns = result.currentMember.redeemedCampaigns
    val referralInformation = result.currentMember.referralInformation

    PaymentOverview(
      memberCharge = result.currentMember.futureCharge?.toMemberCharge(redeemedCampaigns, referralInformation, clock),
      pastCharges = result.currentMember.pastCharges
        .map { it.toMemberCharge(redeemedCampaigns, referralInformation, clock) }
        .reversed(),
      paymentConnection = run {
        val paymentInformation = result.currentMember.paymentInformation
        when (paymentInformation.status) {
          MemberPaymentConnectionStatus.ACTIVE -> {
            if (paymentInformation.connection == null) {
              logcat(LogPriority.ERROR) { "Payment connection is active but connection is null" }
              PaymentConnection.Unknown
            } else {
              PaymentConnection.Active(
                displayName = paymentInformation.connection.displayName,
                displayValue = paymentInformation.connection.descriptor,
              )
            }
          }
          MemberPaymentConnectionStatus.PENDING -> PaymentConnection.Pending
          MemberPaymentConnectionStatus.NEEDS_SETUP -> PaymentConnection.NeedsSetup
          MemberPaymentConnectionStatus.UNKNOWN__ -> PaymentConnection.Unknown
        }
      },
      discounts = result.currentMember.redeemedCampaigns
        .filter { it.type == RedeemedCampaignType.VOUCHER }
        .map {
          Discount(
            code = it.code,
            displayName = it.onlyApplicableToContracts?.firstOrNull()?.exposureDisplayName,
            description = it.description,
            expiredState = Discount.ExpiredState.from(it.expiresAt, clock),
            amount = null,
            isReferral = false,
          )
        } + listOfNotNull(discountFromReferral(result.currentMember.referralInformation)),
    )
  }
}

private fun MemberChargeFragment.toMemberCharge(
  redeemedCampaigns: List<UpcomingPaymentQuery.Data.CurrentMember.RedeemedCampaign>,
  referralInformation: UpcomingPaymentQuery.Data.CurrentMember.ReferralInformation,
  clock: Clock,
) = MemberCharge(
  id = id ?: "",
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
  chargeBreakdowns = contractsChargeBreakdown.map { chargeBreakdown ->
    MemberCharge.ChargeBreakdown(
      contractDisplayName = chargeBreakdown.contract.currentAgreement.productVariant.displayName,
      contractDetails = chargeBreakdown.contract.exposureDisplayName,
      grossAmount = UiMoney.fromMoneyFragment(chargeBreakdown.gross),
      periods = chargeBreakdown.periods.map {
        MemberCharge.ChargeBreakdown.Period(
          amount = UiMoney.fromMoneyFragment(it.amount),
          fromDate = it.fromDate,
          toDate = it.toDate,
          isPreviouslyFailedCharge = it.isPreviouslyFailedCharge,
        )
      },
    )
  },
  discounts = discountBreakdown.map { discountBreakdown ->
    val code = if (discountBreakdown.isReferral) {
      referralInformation.code
    } else if (discountBreakdown.code != null) {
      discountBreakdown.code!!
    } else {
      "-"
    }

    val relatedRedeemedCampaign = redeemedCampaigns.firstOrNull { it.code == discountBreakdown.code }
    Discount(
      code = code,
      displayName = redeemedCampaigns.firstOrNull {
        it.code == discountBreakdown.code
      }?.onlyApplicableToContracts?.firstOrNull()?.exposureDisplayName,
      description = relatedRedeemedCampaign?.description,
      expiredState = Discount.ExpiredState.from(relatedRedeemedCampaign?.expiresAt, clock),
      amount = UiMoney(discountBreakdown.discount.amount.unaryMinus(), discountBreakdown.discount.currencyCode),
      isReferral = discountBreakdown.isReferral,
    )
  },
  settlementAdjustment = settlementAdjustment?.let(UiMoney::fromMoneyFragment),
  carriedAdjustment = carriedAdjustment?.let(UiMoney::fromMoneyFragment),
)

private fun MemberChargeFragment.toFailedCharge(): MemberCharge.FailedCharge? {
  val previousChargesPeriods = contractsChargeBreakdown
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

private fun discountFromReferral(
  referralInformation: UpcomingPaymentQuery.Data.CurrentMember.ReferralInformation,
): Discount? {
  if (referralInformation.referrals.isEmpty()) {
    return null
  }
  return Discount(
    code = referralInformation.code,
    displayName = null,
    description = null,
    expiredState = Discount.ExpiredState.NotExpired,
    amount = UiMoney(
      referralInformation.referrals.sumOf { it.activeDiscount?.amount?.unaryMinus() ?: 0.0 },
      referralInformation.referrals.first().activeDiscount?.currencyCode ?: CurrencyCode.SEK,
    ),
    isReferral = true,
  )
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
