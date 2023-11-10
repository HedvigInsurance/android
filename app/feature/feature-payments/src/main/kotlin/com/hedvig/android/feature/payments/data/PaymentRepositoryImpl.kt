package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.toContractType
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.PaymentDataQuery
import octopus.PaymentHistoryQuery
import octopus.RedeemCampaignCodeMutation
import octopus.type.MemberChargeHistoryEntryStatus
import octopus.type.MemberPaymentConnectionStatus

internal class PaymentRepositoryImpl(
  private val apolloClient: ApolloClient,
) : PaymentRepository {
  override suspend fun getChargeHistory(): Either<ErrorMessage, ChargeHistory> = either {
    apolloClient
      .query(PaymentHistoryQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .map { paymentData ->
        val chargeHistory = paymentData.currentMember.chargeHistory
        ChargeHistory(
          charges = chargeHistory.map { chargeHistoryEntry ->
            ChargeHistory.Charge(
              amount = UiMoney.fromMoneyFragment(chargeHistoryEntry.amount),
              date = chargeHistoryEntry.date,
              paymentStatus = when (chargeHistoryEntry.status) {
                MemberChargeHistoryEntryStatus.PENDING -> ChargeHistory.Charge.PaymentStatus.PENDING
                MemberChargeHistoryEntryStatus.FAILED -> ChargeHistory.Charge.PaymentStatus.FAILED
                MemberChargeHistoryEntryStatus.SUCCESS -> ChargeHistory.Charge.PaymentStatus.SUCCESSFUL
                else -> ChargeHistory.Charge.PaymentStatus.UNKNOWN
              },
            )
          },
        )
      }
      .bind()
  }

  override suspend fun getPaymentData(): Either<ErrorMessage, PaymentData> {
    return either {
      val data = apolloClient
        .query(PaymentDataQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .currentMember

      PaymentData(
        id = data.id,
        upcomingChargeNet = data.upcomingCharge?.net?.let { UiMoney.fromMoneyFragment(it) },
        upcomingChargeDate = data.upcomingCharge?.date,
        monthlyCostNet = UiMoney.fromMoneyFragment(data.insuranceCost.monthlyNet),
        monthlyCostDiscount = data.insuranceCost.monthlyDiscount.takeIf { it.amount > 0 }?.let {
          UiMoney.fromMoneyFragment(data.insuranceCost.monthlyDiscount)
        },
        agreements = data.upcomingCharge?.contractsChargeBreakdown?.map { contractChargeBreakdown ->
          val productVariant = contractChargeBreakdown.contract.currentAgreement.productVariant
          PaymentData.Agreement(
            contractType = productVariant.typeOfContract.toContractType(),
            displayName = productVariant.displayName,
            grossCost = UiMoney.fromMoneyFragment(contractChargeBreakdown.gross),
          )
        } ?: emptyList(),
        paymentConnectionStatus = when (data.paymentInformation.status) {
          MemberPaymentConnectionStatus.ACTIVE -> PaymentData.PaymentConnectionStatus.ACTIVE
          MemberPaymentConnectionStatus.NEEDS_SETUP -> PaymentData.PaymentConnectionStatus.NEEDS_SETUP
          MemberPaymentConnectionStatus.PENDING -> PaymentData.PaymentConnectionStatus.PENDING
          MemberPaymentConnectionStatus.UNKNOWN__ -> PaymentData.PaymentConnectionStatus.UNKNOWN
        },
        paymentDisplayName = data.paymentInformation.connection?.displayName,
        paymentDescriptor = data.paymentInformation.connection?.descriptor,
        redeemedCampaigns = data.redeemedCampaigns.map { redeemedCampaign ->
          PaymentData.RedeemedCampaign(
            id = redeemedCampaign.id,
            displayName = redeemedCampaign.description,
            campaignCode = CampaignCode(redeemedCampaign.code),
            // Use the discounts when the new UI wants to show them broken down into the details
            // discount = redeemedCampaign.discount,
          )
        },
      )
    }
  }

  override suspend fun redeemReferralCode(campaignCode: CampaignCode): Either<RedeemFailure, Unit> {
    return either {
      val response = apolloClient
        .mutation(RedeemCampaignCodeMutation(campaignCode.code))
        .safeExecute()
        .toEither()
        .onLeft { logcat(LogPriority.WARN, it.throwable) { "redeemReferralCode failed. Message:${it.message}" } }
        .mapLeft { RedeemFailure.GenericFailure }
        .bind()
      val userError = response.memberCampaignsRedeem.userError
      ensure(userError == null) {
        val userErrorMessage = userError!!.message
        logcat(LogPriority.ERROR) { "redeemReferralCode failed. User error:$userErrorMessage" }
        if (userErrorMessage != null) {
          RedeemFailure.UserFailure(ErrorMessage(userErrorMessage))
        } else {
          RedeemFailure.GenericFailure
        }
      }
      Unit
    }
  }
}
