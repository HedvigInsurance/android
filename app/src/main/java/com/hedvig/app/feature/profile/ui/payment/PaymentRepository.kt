package com.hedvig.app.feature.profile.ui.payment

import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayoutMethodStatus
import com.hedvig.app.ApolloClientWrapper

class PaymentRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
) {
    private val paymentQuery = PaymentQuery()
    fun payment() = apolloClientWrapper
        .apolloClient
        .query(PaymentQuery())
        .watcher()
        .toFlow()

    suspend fun refresh() = apolloClientWrapper
        .apolloClient
        .query(paymentQuery)
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .await()

    fun writeActivePayoutMethodStatus(status: PayoutMethodStatus) {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
            .read(paymentQuery)
            .execute()

        apolloClientWrapper
            .apolloClient
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
