package com.hedvig.app.data.debit

import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.extensions.toDeferred
import com.hedvig.app.util.extensions.toFlow

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
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .toDeferred()
            .await()

        response.data()?.let { data ->
            val cachedData = apolloClientWrapper
                .apolloClient
                .apolloStore()
                .read(payinStatusQuery)
                .execute()

            val newData = cachedData.copy(payinMethodStatus = data.payinMethodStatus)
            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(payinStatusQuery, newData)
                .execute()
        }
    }
}
