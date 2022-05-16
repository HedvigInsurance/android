package com.hedvig.app.data.debit

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.coroutines.await
import com.apollographql.apollo3.coroutines.toFlow
import com.apollographql.apollo3.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import kotlinx.coroutines.flow.Flow

class PayinStatusRepository(
    private val apolloClient: ApolloClient,
) {
    private val payinStatusQuery = PayinStatusQuery()

    fun payinStatusFlow(): Flow<ApolloResponse<PayinStatusQuery.Data>> = apolloClient
        .query(payinStatusQuery)
        .watcher()
        .toFlow()

    suspend fun refreshPayinStatus() {
        val response = apolloClient
            .query(payinStatusQuery)
            .toBuilder()
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .execute()

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
