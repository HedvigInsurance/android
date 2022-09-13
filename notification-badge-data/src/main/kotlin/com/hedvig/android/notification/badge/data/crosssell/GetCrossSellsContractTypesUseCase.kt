package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.CrossSellsQuery
import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.android.apollo.graphql.type.TypeOfContract
import e

interface GetCrossSellsContractTypesUseCase {
  suspend fun invoke(): Set<TypeOfContract>
}

internal class GetCrossSellsContractTypesUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellsContractTypesUseCase {
  override suspend fun invoke(): Set<TypeOfContract> {
    val result = apolloClient
      // TODO take Locale straight from whatever class owns that functionality after the language APIs are merged in
      .query(CrossSellsQuery(Locale.en_SE))
      .execute()
    val data = result.data
    return if (data != null) {
      data.crossSells
    } else {
      e { "Error when loading potential cross-sells: ${result.errors}" }
      emptySet()
    }
    // todo extract safeQuery into :apollo
    //      .safeQuery()
    //    is QueryResult.Success -> {
    //      result.data.crossSells
    //    }
    //    is QueryResult.Error -> {
    //      e { "Error when loading potential cross-sells: ${result.message}" }
    //      emptySet()
    //    }
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
