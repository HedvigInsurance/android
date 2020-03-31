package com.hedvig.app.feature.adyen

import android.content.Context
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.AdyenPaymentMethodsQuery
import com.hedvig.android.owldroid.graphql.SubmitAdditionalPaymentDetailsMutation
import com.hedvig.android.owldroid.graphql.TokenizePaymentDetailsMutation
import com.hedvig.app.ApolloClientWrapper
import org.json.JSONObject

class AdyenRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun paymentMethodsAsync() = apolloClientWrapper
        .apolloClient
        .query(
            AdyenPaymentMethodsQuery()
        )
        .toDeferred()

    fun tokenizePaymentDetailsAsync(data: JSONObject) = apolloClientWrapper
        .apolloClient
        .mutate(
            TokenizePaymentDetailsMutation(
                data.getJSONObject("paymentMethod").toString(),
                RedirectComponent.getReturnUrl(context)
            )
        )
        .toDeferred()

    fun submitAdditionalPaymentDetailsAsync(data: JSONObject) = apolloClientWrapper
        .apolloClient
        .mutate(SubmitAdditionalPaymentDetailsMutation(data.toString()))
        .toDeferred()
}
