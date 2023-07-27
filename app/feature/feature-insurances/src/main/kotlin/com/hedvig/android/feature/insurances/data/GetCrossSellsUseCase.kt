package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.CrossSalesQuery

internal interface GetCrossSellsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<CrossSalesQuery.Data.CurrentMember.CrossSell>>
}

internal class GetCrossSellsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<CrossSalesQuery.Data.CurrentMember.CrossSell>> {
    return either {
      val result = apolloClient
        .query(CrossSalesQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
      result.currentMember.crossSells
    }
  }
}
