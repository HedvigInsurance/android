package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import octopus.ForeverInformationQuery

internal interface GetForeverInformationUseCase {
  suspend fun invoke(): Either<ErrorMessage, ForeverInformation>
}

internal class GetForeverInformationUseCaseImpl(private val apolloClient: ApolloClient) : GetForeverInformationUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ForeverInformation> {
    return apolloClient
      .query(ForeverInformationQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .map { foreverInformationData ->
        with(foreverInformationData.currentMember) {
          ForeverInformation(
            referralInformation.code,
            UiMoney.fromMoneyFragment(insuranceCost.monthlyDiscount),
            UiMoney.fromMoneyFragment(referralInformation.monthlyDiscountPerReferral),
          )
        }
      }
  }
}

internal data class ForeverInformation(
  val foreverCode: String,
  val currentMonthlyDiscountFromForever: UiMoney,
  val potentialDiscountAmountPerNewReferral: UiMoney,
)
