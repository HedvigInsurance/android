package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.android.owldroid.type.Locale

class MarketingRepository(
    private val apolloClient: ApolloClient,
    private val defaultLocale: Locale
) {
    suspend fun marketingBackground() = apolloClient
        .query(MarketingBackgroundQuery(defaultLocale.rawValue))
        .await()
}
