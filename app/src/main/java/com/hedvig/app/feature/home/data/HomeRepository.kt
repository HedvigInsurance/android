package com.hedvig.app.feature.home.data

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.ApolloClientWrapper

class HomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun homeAsync() = apolloClientWrapper
        .apolloClient
        .query(HomeQuery())
        .toDeferred()
}
