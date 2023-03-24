package com.hedvig.app.feature.adyen

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import giraffe.SubmitAdditionalPaymentDetailsMutation
import giraffe.type.TokenizationResultType
import org.json.JSONObject

class SubmitAdditionalPaymentDetailsUseCase(
  private val apolloClient: ApolloClient,
) {

  data class PaymentResult(
    val code: String,
    val tokenizationResultType: TokenizationResultType,
  )

  sealed class Error {
    data class CheckoutPaymentAction(val action: String) : Error()
    data class ErrorMessage(val message: String?) : Error()
  }

  suspend fun submitAdditionalPaymentDetails(data: JSONObject) = apolloClient
    .mutation(SubmitAdditionalPaymentDetailsMutation(data.toString()))
    .safeExecute()
    .toEither()
    .mapLeft { Error.ErrorMessage(it.message) }
    .flatMap {
      it.submitAdditionalPaymentDetails.asAdditionalPaymentsDetailsResponseAction?.action?.let {
        Error.CheckoutPaymentAction(it).left()
      } ?: it.submitAdditionalPaymentDetails.asAdditionalPaymentsDetailsResponseFinished?.let {
        PaymentResult(it.resultCode, it.tokenizationResult).right()
      } ?: Error.ErrorMessage(null).left()
    }
}
