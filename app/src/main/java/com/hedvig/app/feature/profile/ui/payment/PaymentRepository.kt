package com.hedvig.app.feature.profile.ui.payment

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.graphql.type.PayoutMethodStatus
import com.hedvig.app.util.LocaleManager

class PaymentRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    private val paymentQuery = PaymentQuery(localeManager.defaultLocale())
    fun payment() = apolloClient
        .query(PaymentQuery(localeManager.defaultLocale()))
        .watcher()
        .toFlow()

    suspend fun refresh() = apolloClient
        .query(paymentQuery)
        .httpFetchPolicy(HttpFetchPolicy.NetworkOnly)
        .fetchPolicy(FetchPolicy.NetworkOnly)
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
