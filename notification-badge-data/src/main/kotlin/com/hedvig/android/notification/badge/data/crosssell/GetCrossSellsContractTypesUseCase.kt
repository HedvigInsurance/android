package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.CrossSellsQuery
import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.android.apollo.graphql.type.TypeOfContract
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import e

interface GetCrossSellsContractTypesUseCase {
  suspend fun invoke(): Set<TypeOfContract>
}

internal class GetCrossSellsContractTypesUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getLocale: () -> Locale,
) : GetCrossSellsContractTypesUseCase {
  override suspend fun invoke(): Set<TypeOfContract> {
    return apolloClient
      .query(CrossSellsQuery(getLocale()))
      .safeExecute()
      .toEither()
      .fold(
        { operationResultError ->
          e { "Error when loading potential cross-sells: $operationResultError" }
          emptySet()
        },
        { data ->
          data.crossSells
        },
      )
  }

  private val CrossSellsQuery.Data.crossSells: Set<TypeOfContract>
    get() = activeContractBundles
      .flatMap { contractBundle ->
        contractBundle
          .potentialCrossSells
          .map { it.fragments.crossSellFragment.contractType }
      }
      .toSet()
}
