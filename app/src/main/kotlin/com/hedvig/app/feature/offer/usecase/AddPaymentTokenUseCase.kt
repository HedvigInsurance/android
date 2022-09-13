package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.AddPaymentTokenIdMutation
import com.hedvig.app.feature.adyen.PaymentTokenId
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither

class AddPaymentTokenUseCase(
  private val apolloClient: ApolloClient,
) {

  object Success

  sealed class Error {
    data class CheckoutPaymentAction(val action: String) : Error()
    data class ErrorMessage(val message: String?) : Error()
  }

  suspend operator fun invoke(
    quoteCartId: QuoteCartId,
    paymentTokenId: PaymentTokenId,
  ): Either<Error, Success> {
    val mutation = AddPaymentTokenIdMutation(
      quoteCartId = quoteCartId.id,
      paymentTokenId = paymentTokenId.id,
    )
    return apolloClient.mutation(mutation)
      .safeExecute()
      .toEither()
      .mapLeft { Error.ErrorMessage(it.message) }
      .flatMap {
        it.quoteCart_addPaymentToken.asBasicError?.let {
          Error.ErrorMessage(it.message).left()
        } ?: it.quoteCart_addPaymentToken.asQuoteCart?.let {
          Success.right()
        } ?: Error.ErrorMessage(null).left()
      }
  }
}
