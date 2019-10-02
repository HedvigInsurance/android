package com.hedvig.app.data.debit

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable
import type.DirectDebitStatus

class DirectDebitRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private lateinit var directDebitQuery: DirectDebitQuery

    fun fetchDirectDebit(): Observable<Response<DirectDebitQuery.Data>> {
        directDebitQuery = DirectDebitQuery()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(directDebitQuery).watcher())
    }

    fun refreshDirectdebitStatus(): Observable<Response<DirectDebitQuery.Data>> = Rx2Apollo
        .from(
            apolloClientWrapper.apolloClient
                .query(DirectDebitQuery())
                .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        )

    fun writeDirectDebitStatusToCache(directDebitStatus: DirectDebitStatus) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(directDebitQuery)
            .execute()

        val newData = cachedData.copy(
            directDebitStatus = directDebitStatus
        )

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(directDebitQuery, newData)
            .execute()
    }
}
