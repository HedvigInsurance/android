package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ShouldShowPayoutButtonQuery

internal interface GetShouldShowPayoutUseCase {
  suspend fun invoke(): Either<ErrorMessage, Boolean>
}

/**
 * We do not want to show the payout button at all when there is no payout method connected nor is there a possibility
 * to add one in the member's current state
 */
internal class GetShouldShowPayoutUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetShouldShowPayoutUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Boolean> = either {
    val result = apolloClient
      .query(ShouldShowPayoutButtonQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()

    val paymentMethods = result.currentMember.paymentMethods
    paymentMethods.availableMethods.any { it.supportsPayout } ||
      paymentMethods.defaultPayoutMethod != null ||
      paymentMethods.payoutMethods.isNotEmpty()
  }
}

internal class GetShouldShowPayoutUseCaseDemo : GetShouldShowPayoutUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Boolean> = false.right()
}
