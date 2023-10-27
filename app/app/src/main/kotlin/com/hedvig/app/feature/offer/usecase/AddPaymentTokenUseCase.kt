package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.android.QuoteCartId
import giraffe.AddPaymentTokenIdMutation

class AddPaymentTokenUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    quoteCartId: QuoteCartId,
    paymentTokenId: PaymentTokenId,
  ): Either<ErrorMessage, Unit> {
    val mutation = AddPaymentTokenIdMutation(
      quoteCartId = quoteCartId.id,
      paymentTokenId = paymentTokenId.id,
    )
    return either {
      val result = apolloClient.mutation(mutation).safeExecute().toEither(::ErrorMessage).bind()
      val basicError: AddPaymentTokenIdMutation.AsBasicError? = result.quoteCart_addPaymentToken.asBasicError
      if (basicError != null) {
        raise(ErrorMessage(basicError.message))
      }
      val quoteCart = result.quoteCart_addPaymentToken.asQuoteCart
      ensureNotNull(quoteCart) { ErrorMessage() }
    }
  }
}
