package com.hedvig.app.feature.crossselling.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.CrossSellsQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import e

class GetCrossSellsContractTypesUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend operator fun invoke() = when (
        val result = apolloClient
            .query(CrossSellsQuery(localeManager.defaultLocale()))
            .safeQuery()
    ) {
        is QueryResult.Success -> {
            getCrossSells(result.data)
        }
        is QueryResult.Error -> {
            e { "Error when loading potential cross-sells: ${result.message}" }
            emptySet()
        }
    }

    private fun getCrossSells(
        crossSellData: CrossSellsQuery.Data
    ) = crossSellData
        .activeContractBundles
        .flatMap { contractBundle ->
            contractBundle
                .potentialCrossSells
                .map(CrossSellsQuery.PotentialCrossSell::contractType)
        }
        .toSet()
}
