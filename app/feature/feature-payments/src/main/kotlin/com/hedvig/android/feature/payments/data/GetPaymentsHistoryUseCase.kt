package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ShortPaymentHistoryQuery

internal interface GetPaymentsHistoryUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<PaymentHistoryItem>>
}

internal class GetPaymentsHistoryUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPaymentsHistoryUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<PaymentHistoryItem>> = either {
    val result = apolloClient.query(ShortPaymentHistoryQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()
    val pastCharges = result.currentMember.pastCharges.map {
      it.toPaymentHistoryItem()
    }
    pastCharges
  }
}
