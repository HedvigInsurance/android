package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper

class MarketingRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val defaultLocale: Locale
) {
    suspend fun marketingBackground() = apolloClientWrapper
        .apolloClient
        .query(MarketingBackgroundQuery(defaultLocale.rawValue))
        .await()
}
