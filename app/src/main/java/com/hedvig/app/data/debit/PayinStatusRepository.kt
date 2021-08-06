package com.hedvig.app.data.debit

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PayinStatusRepository(
    private val apolloClient: ApolloClient,
) {
    private val payinStatusQuery = PayinStatusQuery()

    fun payinStatus() = apolloClient
        .query(payinStatusQuery)
        .watcher()
        .toFlow()

    suspend fun refreshPayinStatus() {
        val response = apolloClient
            .query(payinStatusQuery)
            .toBuilder()
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .await()

        response.data?.let { data ->
            val cachedData = apolloClient
                .apolloStore
                .read(payinStatusQuery)
                .execute()

            val newData = cachedData.copy(payinMethodStatus = data.payinMethodStatus)
            apolloClient
                .apolloStore
                .writeAndPublish(payinStatusQuery, newData)
                .execute()
        }
    }
}
