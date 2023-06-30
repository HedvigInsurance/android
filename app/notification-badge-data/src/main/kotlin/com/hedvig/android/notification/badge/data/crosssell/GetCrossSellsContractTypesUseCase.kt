package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import giraffe.CrossSellsQuery
import giraffe.type.TypeOfContract
import slimber.log.d
import slimber.log.e

interface GetCrossSellsContractTypesUseCase {
  suspend fun invoke(): Set<TypeOfContract>
}

internal class GetCrossSellsContractTypesUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) : GetCrossSellsContractTypesUseCase {
  override suspend fun invoke(): Set<TypeOfContract> {
    return apolloClient
      .query(CrossSellsQuery(languageService.getGraphQLLocale()))
      .safeExecute()
      .toEither()
      .fold(
        { operationResultError ->
          // This runs on app startup, where we may be logged out. If this is the case, no need to log error
          if (operationResultError.message?.contains("Must be logged in") == false) {
            e { "Error when loading potential cross-sells: $operationResultError" }
          } else {
            d { "Error when loading potential cross-sells: $operationResultError" }
          }
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
