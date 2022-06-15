package com.hedvig.app.feature.adyen

import android.content.Context
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.hedvig.android.owldroid.graphql.AdyenPaymentMethodsQuery
import com.hedvig.android.owldroid.graphql.AdyenPayoutMethodsQuery
import com.hedvig.android.owldroid.graphql.TokenizePayoutDetailsMutation
import org.json.JSONObject

class AdyenRepository(
    private val apolloClient: ApolloClient,
    private val context: Context,
) {

    suspend fun paymentMethods(): ApolloResponse<AdyenPaymentMethodsQuery.Data> = apolloClient
        .query(AdyenPaymentMethodsQuery())
        .execute()

    suspend fun payoutMethods(): ApolloResponse<AdyenPayoutMethodsQuery.Data> = apolloClient
        .query(AdyenPayoutMethodsQuery())
        .execute()

    suspend fun tokenizePayoutDetails(data: JSONObject) = apolloClient
        .mutation(
            TokenizePayoutDetailsMutation(
                data.getJSONObject("paymentMethod").toString(),
                RedirectComponent.getReturnUrl(context)
            )
        )
        .execute()

    suspend fun paymentMethodsResponse(): PaymentMethodsApiResponse? {
        return paymentMethods()
            .data
            ?.availablePaymentMethods
            ?.paymentMethodsResponse
    }
}
