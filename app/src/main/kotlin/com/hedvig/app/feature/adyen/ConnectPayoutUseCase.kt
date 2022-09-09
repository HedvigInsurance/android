package com.hedvig.app.feature.adyen

import android.content.Context
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.TokenizePayoutDetailsMutation
import com.hedvig.android.apollo.graphql.type.TokenizationResultType
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toEither
import org.json.JSONObject

class ConnectPayoutUseCase(
  private val apolloClient: ApolloClient,
  private val context: Context,
) {

  data class PayOutResult(
    val code: String,
    val tokenizationResultType: TokenizationResultType,
  )

  sealed interface Error {
    data class CheckoutPaymentAction(val action: String) : Error
    data class ErrorMessage(val message: String?) : Error
  }

  suspend fun connectPayout(data: JSONObject) = apolloClient
    .mutation(createTokenizePayoutDetailsMutation(data))
    .safeQuery()
    .toEither { Error.ErrorMessage(it) }
    .flatMap {
      it.tokenizePayoutDetails?.asTokenizationResponseAction?.let {
        Error.CheckoutPaymentAction(it.action).left()
      } ?: it.tokenizePayoutDetails?.asTokenizationResponseFinished?.let {
        PayOutResult(it.resultCode, it.tokenizationResult).right()
      } ?: Error.ErrorMessage(null).left()
    }

  private fun createTokenizePayoutDetailsMutation(data: JSONObject) = TokenizePayoutDetailsMutation(
    data.getJSONObject("paymentMethod").toString(),
    RedirectComponent.getReturnUrl(context),
  )
}
