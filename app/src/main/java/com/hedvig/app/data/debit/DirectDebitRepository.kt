package com.hedvig.app.data.debit

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.app.ApolloClientWrapper
import kotlinx.coroutines.flow.Flow

class DirectDebitRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    private lateinit var directDebitQuery: DirectDebitQuery

    fun directDebit(): Flow<Response<DirectDebitQuery.Data>> {
        directDebitQuery = DirectDebitQuery()

        return apolloClientWrapper
            .apolloClient
            .query(directDebitQuery)
            .watcher()
            .toFlow()
    }

    suspend fun refreshDirectDebitStatus() {
        val bankAccountQuery = DirectDebitQuery()

        val response = apolloClientWrapper
            .apolloClient
            .query(bankAccountQuery)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .toDeferred()
            .await()

        response.data()?.let { newData ->
            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(directDebitQuery, newData)
        }
    }
}
