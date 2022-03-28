package com.hedvig.app.feature.adyen

import android.content.Context
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.AdyenPaymentMethodsQuery
import com.hedvig.android.owldroid.graphql.AdyenPayoutMethodsQuery
import com.hedvig.android.owldroid.graphql.SubmitAdditionalPaymentDetailsMutation
import com.hedvig.android.owldroid.graphql.TokenizePaymentDetailsMutation
import com.hedvig.android.owldroid.graphql.TokenizePayoutDetailsMutation
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature
import org.json.JSONObject

class AdyenRepository(
    private val apolloClient: ApolloClient,
    private val context: Context,
    private val featureManager: FeatureManager,
) {
    suspend fun paymentMethods() = apolloClient
        .query(AdyenPaymentMethodsQuery())
        .await()

    suspend fun payoutMethods() = apolloClient
        .query(AdyenPayoutMethodsQuery())
        .await()

    suspend fun tokenizePaymentDetails(data: JSONObject) = apolloClient
        .mutate(
            TokenizePaymentDetailsMutation(
                data.getJSONObject("paymentMethod").toString(),
                RedirectComponent.getReturnUrl(context)
            )
        )
        .await()

    suspend fun tokenizePayoutDetails(data: JSONObject) = apolloClient
        .mutate(
            TokenizePayoutDetailsMutation(
                data.getJSONObject("paymentMethod").toString(),
                RedirectComponent.getReturnUrl(context)
            )
        )
        .await()

    suspend fun submitAdditionalPaymentDetails(data: JSONObject) = apolloClient
        .mutate(SubmitAdditionalPaymentDetailsMutation(data.toString()))
        .await()

    suspend fun paymentMethodsResponse(): PaymentMethodsApiResponse? {
        return if (featureManager.isFeatureEnabled(Feature.CONNECT_PAYMENT_AT_SIGN)) {
            paymentMethods()
                .data
                ?.availablePaymentMethods
                ?.paymentMethodsResponse
        } else {
            null
        }
    }
}
