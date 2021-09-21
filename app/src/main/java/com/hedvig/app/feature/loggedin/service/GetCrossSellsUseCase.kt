package com.hedvig.app.feature.loggedin.service

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.CrossSellsQuery
import com.hedvig.app.util.apollo.safeQuery

class GetCrossSellsUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke() = apolloClient
        .query(CrossSellsQuery())
        .safeQuery()
}
