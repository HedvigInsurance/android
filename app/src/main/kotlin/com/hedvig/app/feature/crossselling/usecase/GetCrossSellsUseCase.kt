package com.hedvig.app.feature.crossselling.usecase

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.CrossSellsQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.util.LocaleManager
import e

class GetCrossSellsUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {
  suspend operator fun invoke() = when (
    val result = apolloClient
      .query(CrossSellsQuery(localeManager.defaultLocale()))
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
