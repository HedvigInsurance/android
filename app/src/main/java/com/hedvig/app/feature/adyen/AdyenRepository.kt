package com.hedvig.app.feature.adyen

import android.content.Context
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.coroutines.await
import com.hedvig.android.owldroid.graphql.AdyenPaymentMethodsQuery
import com.hedvig.android.owldroid.graphql.AdyenPayoutMethodsQuery
import com.hedvig.android.owldroid.graphql.TokenizePayoutDetailsMutation
import org.json.JSONObject

class AdyenRepository(
    private val apolloClient: ApolloClient,
    private val context: Context,
) {

    suspend fun paymentMethods() = apolloClient
        .query(AdyenPaymentMethodsQuery())
        .await()

    suspend fun payoutMethods() = apolloClient
        .query(AdyenPayoutMethodsQuery())
        .await()

    suspend fun tokenizePayoutDetails(data: JSONObject) = apolloClient
        .mutate(
            TokenizePayoutDetailsMutation(
                data.getJSONObject("paymentMethod").toString(),
                RedirectComponent.getReturnUrl(context)
            )
        )
        .await()

    suspend fun paymentMethodsResponse(): PaymentMethodsApiResponse? {
        return paymentMethods()
            .data
            ?.availablePaymentMethods
            ?.paymentMethodsResponse
    }
}
