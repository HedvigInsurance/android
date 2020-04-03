package com.hedvig.app.feature.loggedin.ui

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.FeaturesQuery
import com.hedvig.app.ApolloClientWrapper

class FeatureRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun featuresAsync() = apolloClientWrapper
        .apolloClient
        .query(FeaturesQuery())
        .toDeferred()
}
