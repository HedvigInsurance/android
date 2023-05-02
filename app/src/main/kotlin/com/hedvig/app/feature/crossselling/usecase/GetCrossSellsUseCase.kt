package com.hedvig.app.feature.crossselling.usecase

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import octopus.CrossSalesQuery

class GetCrossSellsUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, List<CrossSellData>> {
    return either {
      val result = apolloClient
        .query(CrossSalesQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
      getCrossSellsContractTypes(result)
    }
  }

  private fun getCrossSellsContractTypes(crossSellData: CrossSalesQuery.Data) = crossSellData
    .currentMember
    .crossSells
    .map { CrossSellData.from(it) }
}
