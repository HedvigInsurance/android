package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import octopus.ForeverInformationQuery
import octopus.type.MemberReferralStatus

internal interface GetForeverInformationUseCase {
  suspend fun invoke(): Either<ErrorMessage, ForeverInformation>
}

internal class GetForeverInformationUseCaseImpl(private val apolloClient: ApolloClient) : GetForeverInformationUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ForeverInformation> {
    return apolloClient
      .query(ForeverInformationQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .map { foreverInformationData ->
        with(foreverInformationData.currentMember) {
          val filteredReferrals = referralInformation.referrals
            .filter { it.status == MemberReferralStatus.ACTIVE && it.activeDiscount != null }
          val amount = filteredReferrals
            .map { it.activeDiscount?.amount ?: 0.0 }
            .sum()
          val currencyCode =
            filteredReferrals.firstOrNull()?.activeDiscount?.currencyCode?.let { UiCurrencyCode.fromCurrencyCode(it) }
              ?: UiCurrencyCode.SEK
          val amountFromReferrals = UiMoney(amount, currencyCode)
          ForeverInformation(
            foreverCode = referralInformation.code,
            currentMonthlyDiscountFromReferrals = amountFromReferrals,
            potentialDiscountAmountPerNewReferral = UiMoney.fromMoneyFragment(referralInformation.monthlyDiscountPerReferral),
            numberOfReferrals = referralInformation.referrals.filter { it.status == MemberReferralStatus.ACTIVE }.size,
            referredBy = with(referralInformation.referredBy) {
              if (this != null && status == MemberReferralStatus.ACTIVE && activeDiscount != null)
                ReferredByInfo(
                  name = name,
                  activeDiscount = UiMoney.fromMoneyFragment(activeDiscount),
                ) else null
            },
          )
        }
      }
  }
}

internal data class ForeverInformation(
  val foreverCode: String,
  val currentMonthlyDiscountFromReferrals: UiMoney,
  val potentialDiscountAmountPerNewReferral: UiMoney,
  val numberOfReferrals: Int,
  val referredBy: ReferredByInfo?,
)

internal data class ReferredByInfo(
  val name: String,
  val activeDiscount: UiMoney,
)
