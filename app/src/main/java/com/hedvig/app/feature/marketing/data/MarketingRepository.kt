package com.hedvig.app.feature.marketing.data

import android.content.Context
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale

class MarketingRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun marketingBackgroundAsync() = apolloClientWrapper
        .apolloClient
        .query(MarketingBackgroundQuery(defaultLocale(context).rawValue))
        .toDeferred()
}
