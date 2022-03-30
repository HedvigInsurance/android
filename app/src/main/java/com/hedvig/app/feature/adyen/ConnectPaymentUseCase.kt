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
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject

class ConnectPaymentUseCase(
    private val apolloClient: ApolloClient,
    private val context: Context,
    private val featureManager: FeatureManager,
    private val marketManager: MarketManager,
    private val graphQLQueryHandler: GraphQLQueryHandler,
) {

    sealed interface Error {
        data class CheckoutPaymentAction(val action: String) : Error
        data class ErrorMessage(val message: String?) : Error
    }

    suspend fun getPaymentTokenId(data: JSONObject): Either<Error, PaymentTokenId> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            connectPayment(data)
        } else {
            tokenizePaymentDetails(data)
        }
    }

    private suspend fun connectPayment(data: JSONObject): Either<Error, PaymentTokenId> = graphQLQueryHandler
        .graphQLQuery(
            query = ConnectPaymentMutation.QUERY_DOCUMENT,
            variables = createConnectPaymentVariables(data),
            files = emptyList()
        )
        .toEither()
        .mapLeft { Error.ErrorMessage(null) }
        .flatMap { jsonResponse ->
            val id = jsonResponse
                .getJSONObject("data")
                .getJSONObject("paymentConnection_connectPayment")
                .getString("paymentTokenId")
            PaymentTokenId(id).right()
        }

    private fun createConnectPaymentVariables(data: JSONObject): JSONObject? {
        val market = when (marketManager.market) {
            Market.SE -> com.hedvig.android.owldroid.type.Market.SWEDEN
            Market.NO -> com.hedvig.android.owldroid.type.Market.NORWAY
            Market.DK -> com.hedvig.android.owldroid.type.Market.DENMARK
            Market.FR -> com.hedvig.android.owldroid.type.Market.UNKNOWN__
            null -> com.hedvig.android.owldroid.type.Market.UNKNOWN__
        }.toString()

        return buildJsonObject {
            put("market", market)
            put("returnUrl", RedirectComponent.getReturnUrl(context))
        }
            .toString()
            .let { JSONObject(it) }.put("paymentMethodDetails", data.getJSONObject("paymentMethod"))
    }

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
