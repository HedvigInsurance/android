package com.hedvig.app.feature.crossselling.usecase

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import giraffe.CrossSellsQuery
import slimber.log.e

class GetCrossSellsUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend operator fun invoke() = when (
    val result = apolloClient
      .query(CrossSellsQuery(languageService.getGraphQLLocale()))
      .safeExecute()
  ) {
    is OperationResult.Success -> {
      getCrossSellsContractTypes(result.data)
    }
    is OperationResult.Error -> {
      e { "Error when loading potential cross-sells: ${result.message}" }
      emptySet()
    }
  }

  private fun getCrossSellsContractTypes(
    crossSellData: CrossSellsQuery.Data,
  ) = crossSellData
    .activeContractBundles
    .flatMap { contractBundle ->
      contractBundle.potentialCrossSells
    }.map {
      CrossSellData.from(it.fragments.crossSellFragment)
    }
}
