package com.hedvig.app.data.debit

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.ApolloClientWrapper
import kotlinx.coroutines.flow.Flow

class DirectDebitRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    private lateinit var payinStatusQuery: PayinStatusQuery

    fun payinStatus(): Flow<Response<PayinStatusQuery.Data>> {
        payinStatusQuery = PayinStatusQuery()

        return apolloClientWrapper
            .apolloClient
            .query(payinStatusQuery)
            .watcher()
            .toFlow()
    }

    suspend fun refreshPayinStatus() {
        val response = apolloClientWrapper
            .apolloClient
            .query(PayinStatusQuery())
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .toDeferred()
            .await()

        response.data()?.let { newData ->
            apolloClientWrapper
                .apolloClient
                .apolloStore
                .writeAndPublish(payinStatusQuery, newData)
        }
    }
}
