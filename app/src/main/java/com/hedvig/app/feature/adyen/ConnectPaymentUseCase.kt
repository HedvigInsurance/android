package com.hedvig.app.feature.adyen

import android.content.Context
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import com.adyen.checkout.redirect.RedirectComponent
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.owldroid.graphql.ConnectPaymentMutation
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import com.hedvig.app.util.apollo.toEither
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject

class ConnectPaymentUseCase(
  private val context: Context,
  private val marketManager: MarketManager,
  private val graphQLQueryHandler: GraphQLQueryHandler,
) {

  sealed interface Error {
    data class CheckoutPaymentAction(val action: String) : Error
    data class ErrorMessage(val message: String?) : Error
  }

  suspend fun getPaymentTokenId(data: JSONObject): Either<Error, PaymentTokenId> {
    return connectPayment(data)
  }

  private suspend fun connectPayment(data: JSONObject): Either<Error, PaymentTokenId> = graphQLQueryHandler
    .graphQLQuery(
      query = ConnectPaymentMutation.OPERATION_DOCUMENT,
      variables = createConnectPaymentVariables(data),
      files = emptyList(),
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
      Market.SE -> com.hedvig.android.owldroid.graphql.type.Market.SWEDEN
      Market.NO -> com.hedvig.android.owldroid.graphql.type.Market.NORWAY
      Market.DK -> com.hedvig.android.owldroid.graphql.type.Market.DENMARK
      Market.FR -> com.hedvig.android.owldroid.graphql.type.Market.UNKNOWN__
      null -> com.hedvig.android.owldroid.graphql.type.Market.UNKNOWN__
    }.toString()

    return buildJsonObject {
      put("market", market)
      put("returnUrl", RedirectComponent.getReturnUrl(context))
    }
      .toString()
      .let { JSONObject(it) }.put("paymentMethodDetails", data.getJSONObject("paymentMethod"))
  }
}
