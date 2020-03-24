package com.hedvig.app.feature.adyen

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.AdyenPaymentMethodsQuery
import com.hedvig.android.owldroid.graphql.SubmitAdditionalPaymentDetailsMutation
import com.hedvig.android.owldroid.graphql.TokenizePaymentDetailsMutation
import com.hedvig.app.ApolloClientWrapper
import org.json.JSONObject

class AdyenRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun paymentMethodsAsync() = apolloClientWrapper
        .apolloClient
        .query(
            AdyenPaymentMethodsQuery()
        )
        .toDeferred()

    fun tokenizePaymentDetailsAsync(data: JSONObject) = apolloClientWrapper
        .apolloClient
        .mutate(TokenizePaymentDetailsMutation(data.toString()))
        .toDeferred()

    fun submitAdditionalPaymentDetailsAsync(data: JSONObject) = apolloClientWrapper
        .apolloClient
        .mutate(SubmitAdditionalPaymentDetailsMutation(data.toString()))
        .toDeferred()
}
