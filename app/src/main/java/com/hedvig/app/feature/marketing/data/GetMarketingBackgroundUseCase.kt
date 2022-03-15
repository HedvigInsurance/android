package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class GetMarketingBackgroundUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend operator fun invoke() = apolloClient
        .query(MarketingBackgroundQuery(localeManager.defaultLocale().rawValue))
        .safeQuery()
        .toEither()
        .map { it.appMarketingImages.firstOrNull() }
}
