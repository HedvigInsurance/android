package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.collections.immutable.toPersistentList
import octopus.UpcomingPaymentQuery
import octopus.fragment.MemberChargeFragment
import octopus.type.CurrencyCode
import octopus.type.MemberChargeStatus
import octopus.type.MemberPaymentConnectionStatus

internal interface GetUpcomingPaymentUseCase {
  suspend fun invoke(): Either<ErrorMessage, PaymentOverview>
}

internal data class GetUpcomingPaymentUseCaseImpl(
  val apolloClient: ApolloClient,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> = either {
    val result = apolloClient.query(UpcomingPaymentQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    PaymentOverview(
      memberCharge = result.currentMember.futureCharge?.toMemberCharge(),
      pastCharges = result.currentMember.pastCharges.map { it.toMemberCharge() }.reversed(),
      paymentConnection = PaymentConnection(
        connectionInfo = result.currentMember.paymentInformation.connection?.let {
          PaymentConnection.ConnectionInfo(
            displayName = it.displayName,
            displayValue = it.descriptor,
          )
        },
        status = when (result.currentMember.paymentInformation.status) {
          MemberPaymentConnectionStatus.ACTIVE -> PaymentConnection.PaymentConnectionStatus.ACTIVE
          MemberPaymentConnectionStatus.PENDING -> PaymentConnection.PaymentConnectionStatus.PENDING
          MemberPaymentConnectionStatus.NEEDS_SETUP -> PaymentConnection.PaymentConnectionStatus.NEEDS_SETUP
          MemberPaymentConnectionStatus.UNKNOWN__ -> PaymentConnection.PaymentConnectionStatus.UNKNOWN
        },
      ),
      discounts = result.currentMember.redeemedCampaigns.map {
        Discount(
          code = it.code,
          displayName = it.onlyApplicableToContracts?.firstOrNull()?.exposureDisplayName,
          description = it.description,
          expiresAt = it.expiresAt,
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
  chargeBreakdowns = contractsChargeBreakdown.map {
    MemberCharge.ChargeBreakdown(
      contractDisplayName = it.contract.currentAgreement.productVariant.displayName,
      contractDetails = it.contract.exposureDisplayName,
      grossAmount = UiMoney.fromMoneyFragment(it.gross),
      periods = it.periods.map {
        MemberCharge.ChargeBreakdown.Period(
          amount = UiMoney.fromMoneyFragment(it.amount),
          fromDate = it.fromDate,
          toDate = it.toDate,
          isPreviouslyFailedCharge = it.isPreviouslyFailedCharge,
        )
      }.toPersistentList(),
    )
  }.toPersistentList(),
  discounts = discountBreakdown.mapNotNull { discountBreakdown ->
    if (discountBreakdown.isReferral) {
      discountFromReferral(referralInformation)
    } else {
      Discount(
        code = discountBreakdown.code ?: "-",
        displayName = redeemedCampaigns.firstOrNull {
          it.code == discountBreakdown.code
        }?.onlyApplicableToContracts?.firstOrNull()?.exposureDisplayName,
        description = redeemedCampaigns.firstOrNull { it.code == discountBreakdown.code }?.description,
        expiresAt = redeemedCampaigns.firstOrNull { it.code == discountBreakdown.code }?.expiresAt,
        amount = UiMoney(discountBreakdown.discount.amount.unaryMinus(), discountBreakdown.discount.currencyCode),
        isReferral = false,
      )
    }
  },
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
    expiresAt = null,
    amount = UiMoney(
      referralInformation.referrals.sumOf { it.activeDiscount?.amount?.unaryMinus() ?: 0.0 },
      referralInformation.referrals.first().activeDiscount?.currencyCode ?: CurrencyCode.SEK,
    ),
    isReferral = true,
  )
}
