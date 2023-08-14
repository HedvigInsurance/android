package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import giraffe.CrossSellsQuery
import giraffe.type.TypeOfContract

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
        { operationResultError: OperationResult.Error ->
          logcat(LogPriority.DEBUG, operationResultError.throwable) {
            "Error when loading potential cross-sells: $operationResultError"
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
