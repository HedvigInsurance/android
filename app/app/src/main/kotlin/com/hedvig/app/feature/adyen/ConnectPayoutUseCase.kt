package com.hedvig.app.feature.adyen

import android.content.Context
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.adyen.checkout.redirect.RedirectComponent
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import giraffe.TokenizePayoutDetailsMutation
import giraffe.type.TokenizationResultType
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

  suspend fun connectPayout(data: JSONObject): Either<Error, PayOutResult> = apolloClient
    .mutation(createTokenizePayoutDetailsMutation(data))
    .safeExecute()
    .toEither { message, _ -> Error.ErrorMessage(message) }
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
