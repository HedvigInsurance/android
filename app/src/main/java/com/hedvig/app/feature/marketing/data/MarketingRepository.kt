package com.hedvig.app.feature.marketing.data

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.util.apollo.defaultLocale

class MarketingRepository(
    private val apolloClient: ApolloClient,
    private val context: Context,
) {
    suspend fun marketingBackground() = apolloClient
        .query(MarketingBackgroundQuery(defaultLocale(context).rawValue))
        .await()
}
