package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SignQuoteCartMutation
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.safeQuery

class SignQuotesUseCase(
    private val apolloClient: ApolloClient,
) {

    object Success

    suspend fun signQuotesAndClearCache(
        quoteCartId: QuoteCartId?,
        quoteIds: List<String>
    ): Either<ErrorMessage, Success> = either {
        ensureNotNull(quoteCartId) { ErrorMessage("Quote cart id not found") }

        val result = mutateQuoteCart(quoteCartId, quoteIds).bind()
        val errorMessage = result.quoteCartStartCheckout.asBasicError?.message

        ensure(errorMessage == null) { ErrorMessage(errorMessage) }

        Success
    }

    private suspend fun mutateQuoteCart(
        quoteCartId: QuoteCartId,
        quoteIds: List<String>
    ): Either<ErrorMessage, SignQuoteCartMutation.Data> = apolloClient
        .mutate(SignQuoteCartMutation(quoteCartId.id, quoteIds))
        .safeQuery()
        .toEither()
        .mapLeft { ErrorMessage(it.message) }
}
