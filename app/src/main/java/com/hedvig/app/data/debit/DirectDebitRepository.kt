package com.hedvig.app.data.debit

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable

class DirectDebitRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private lateinit var directDebitQuery: DirectDebitQuery

    fun fetchDirectDebit(): Observable<Response<DirectDebitQuery.Data>> {
        directDebitQuery = DirectDebitQuery
            .builder()
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(directDebitQuery).watcher())
    }

    fun refreshDirectdebitStatus(): Observable<Response<DirectDebitQuery.Data>> {
        val bankAccountQuery = DirectDebitQuery
            .builder()
            .build()

        return Rx2Apollo
            .from(
                apolloClientWrapper.apolloClient
                    .query(bankAccountQuery)
                    .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            )
    }

    fun writeDirectDebitStatusToCache(directDebitStatus: DirectDebitStatus) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(directDebitQuery)
            .execute()

        val newData = cachedData
            .toBuilder()
            .directDebitStatus(directDebitStatus)
            .build()

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(directDebitQuery, newData)
            .execute()
    }
}
