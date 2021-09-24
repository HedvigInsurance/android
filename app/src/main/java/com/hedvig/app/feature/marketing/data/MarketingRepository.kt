package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.util.LocaleManager
import javax.inject.Inject

class MarketingRepository @Inject constructor(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend fun marketingBackground() = apolloClient
        .query(MarketingBackgroundQuery(localeManager.defaultLocale().rawValue))
        .await()
}
