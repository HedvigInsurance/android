package com.hedvig.app.data.debit

import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.ApolloClientWrapper

class PayinStatusRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    private val payinStatusQuery = PayinStatusQuery()

    fun payinStatus() = apolloClientWrapper
        .apolloClient
        .query(payinStatusQuery)
        .watcher()
        .toFlow()

    suspend fun refreshPayinStatus() {
        val response = apolloClientWrapper
            .apolloClient
            .query(payinStatusQuery)
            .toBuilder()
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .await()

        response.data?.let { data ->
            val cachedData = apolloClientWrapper
                .apolloClient
                .apolloStore
                .read(payinStatusQuery)
                .execute()

            val newData = cachedData.copy(payinMethodStatus = data.payinMethodStatus)
            apolloClientWrapper
                .apolloClient
                .apolloStore
                .writeAndPublish(payinStatusQuery, newData)
                .execute()
        }
    }
}
