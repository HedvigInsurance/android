package com.hedvig.app.feature.loggedin.ui

import com.hedvig.android.owldroid.graphql.FeaturesQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred

class FeatureRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun featuresAsync() = apolloClientWrapper
        .apolloClient
        .query(FeaturesQuery())
        .toDeferred()
}
