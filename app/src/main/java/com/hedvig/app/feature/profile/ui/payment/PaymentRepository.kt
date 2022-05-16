package com.hedvig.app.feature.profile.ui.payment

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.cache.http.HttpCachePolicy
import com.apollographql.apollo3.coroutines.await
import com.apollographql.apollo3.coroutines.toFlow
import com.apollographql.apollo3.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayoutMethodStatus
import com.hedvig.app.util.LocaleManager

class PaymentRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    private val paymentQuery = PaymentQuery(localeManager.defaultLocale())
    fun payment() = apolloClient
        .query(PaymentQuery(localeManager.defaultLocale()))
        .watcher()
        .toFlow()

    suspend fun refresh() = apolloClient
        .query(paymentQuery)
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .execute()

    fun writeActivePayoutMethodStatus(status: PayoutMethodStatus) {
        val cachedData = apolloClient
            .apolloStore
            .read(paymentQuery)
            .execute()

        apolloClient
            .apolloStore
            .writeAndPublish(
                paymentQuery,
                cachedData.copy(
                    activePayoutMethods = PaymentQuery.ActivePayoutMethods(status = status)
                )
            )
            .execute()
    }
}
