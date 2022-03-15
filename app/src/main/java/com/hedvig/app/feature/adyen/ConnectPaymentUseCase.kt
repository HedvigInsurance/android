package com.hedvig.app.feature.adyen

import android.content.Context
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ConnectPaymentMutation
import com.hedvig.android.owldroid.graphql.TokenizePaymentDetailsMutation
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import org.json.JSONObject

class ConnectPaymentUseCase(
    private val apolloClient: ApolloClient,
    private val context: Context,
    private val featureManager: FeatureManager,
    private val marketManager: MarketManager,
) {

    sealed class Error {
        data class CheckoutPaymentAction(val action: String) : Error()
        data class ErrorMessage(val message: String?) : Error()
    }

    suspend fun getPaymentTokenId(data: JSONObject): Either<Error, PaymentTokenId> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            connectPayment(data)
        } else {
            tokenizePaymentDetails(data)
        }
    }

    private suspend fun connectPayment(data: JSONObject): Either<Error, PaymentTokenId> = apolloClient
        .mutate(createConnectPaymentMutation(data))
        .safeQuery()
        .toEither()
        .mapLeft { Error.ErrorMessage(it.message) }
        .flatMap {
            it.paymentConnection_connectPayment.asConnectPaymentFinished?.let {
                PaymentTokenId(it.paymentTokenId).right()
            } ?: it.paymentConnection_connectPayment.asActionRequired?.action?.let {
                Error.CheckoutPaymentAction(it).left()
            } ?: Error.ErrorMessage(null).left()
        }

    private fun createConnectPaymentMutation(data: JSONObject) = ConnectPaymentMutation(
        paymentMethodDetails = data.getJSONObject("paymentMethod").toString(),
        returnUrl = RedirectComponent.getReturnUrl(context),
        market = when (marketManager.market) {
            Market.SE -> com.hedvig.android.owldroid.type.Market.SWEDEN
            Market.NO -> com.hedvig.android.owldroid.type.Market.NORWAY
            Market.DK -> com.hedvig.android.owldroid.type.Market.DENMARK
            Market.FR -> com.hedvig.android.owldroid.type.Market.UNKNOWN__
            null -> com.hedvig.android.owldroid.type.Market.UNKNOWN__
        }
    )

    private suspend fun tokenizePaymentDetails(data: JSONObject): Either<Error, PaymentTokenId> = apolloClient
        .mutate(createTokenizePaymentMutation(data))
        .safeQuery()
        .toEither()
        .mapLeft { Error.ErrorMessage(it.message) }
        .flatMap {
            it.tokenizePaymentDetails?.asTokenizationResponseFinished?.let {
                PaymentTokenId(it.resultCode).right()
            } ?: it.tokenizePaymentDetails?.asTokenizationResponseAction?.let {
                Error.CheckoutPaymentAction(it.action).left()
            } ?: Error.ErrorMessage(null).left()
        }

    private fun createTokenizePaymentMutation(data: JSONObject) = TokenizePaymentDetailsMutation(
        data.getJSONObject("paymentMethod").toString(),
        RedirectComponent.getReturnUrl(context)
    )
}
