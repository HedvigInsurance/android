package com.hedvig.app.feature.adyen.payin

import android.content.Context
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.AdyenPaymentMethodsQuery
import com.hedvig.android.owldroid.graphql.SubmitAdditionalPaymentDetailsMutation
import com.hedvig.android.owldroid.graphql.TokenizePaymentDetailsMutation
import com.hedvig.app.ApolloClientWrapper
import org.json.JSONObject

class AdyenRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    suspend fun paymentMethods() = apolloClientWrapper
        .apolloClient
        .query(
            AdyenPaymentMethodsQuery()
        )
        .await()

    suspend fun tokenizePaymentDetails(data: JSONObject) = apolloClientWrapper
        .apolloClient
        .mutate(
            TokenizePaymentDetailsMutation(
                data.getJSONObject("paymentMethod").toString(),
                RedirectComponent.getReturnUrl(context)
            )
        )
        .await()

    suspend fun submitAdditionalPaymentDetails(data: JSONObject) = apolloClientWrapper
        .apolloClient
        .mutate(SubmitAdditionalPaymentDetailsMutation(data.toString()))
        .await()
}
