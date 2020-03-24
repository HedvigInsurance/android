package com.hedvig.app.feature.adyen

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.AdyenPaymentMethodsQuery
import com.hedvig.app.ApolloClientWrapper

class AdyenRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun paymentMethodsAsync() = apolloClientWrapper
        .apolloClient
        .query(
            AdyenPaymentMethodsQuery()
        )
        .toDeferred()
}
