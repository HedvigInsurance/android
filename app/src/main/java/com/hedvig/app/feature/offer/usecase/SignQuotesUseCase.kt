package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import arrow.core.left
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SignQuoteCartMutation
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager

class SignQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val cacheManager: CacheManager,
    private val featureManager: FeatureManager,
    private val offerRepository: OfferRepository
) {

    sealed class SignQuoteResult {
        object StartSimpleSign : SignQuoteResult()
        data class StartSwedishBankId(
            val autoStartToken: String?,
        ) : SignQuoteResult()
    }

    suspend fun signQuotesAndClearCache(
        quoteIds: List<String>,
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId?
    ): Either<ErrorMessage, SignQuoteResult> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            signQuoteCart(quoteCartId)
        } else {
            signQuotes(quoteIds)
        }
    }

    private suspend fun signQuoteCart(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId?
    ): Either<ErrorMessage, SignQuoteResult> = either {
        ensureNotNull(quoteCartId) { ErrorMessage("Quote cart id not found") }

        val quoteIds = offerRepository.getQuoteIds(quoteCartId).bind()
        val result = mutateQuoteCart(quoteCartId, quoteIds).bind()

        val errorMessage = result.quoteCartStartCheckout.asBasicError?.message
        ensure(errorMessage == null) { ErrorMessage(errorMessage) }

        SignQuoteResult.StartSwedishBankId(null)
    }

    private suspend fun mutateQuoteCart(
        quoteCartId: CreateQuoteCartUseCase.QuoteCartId,
        quoteIds: List<String>
    ): Either<ErrorMessage, SignQuoteCartMutation.Data> = apolloClient
        .mutate(SignQuoteCartMutation(quoteCartId.id, quoteIds))
        .safeQuery()
        .toEither()
        .mapLeft { ErrorMessage(it.message) }

    private suspend fun signQuotes(
        quoteIds: List<String>
    ): Either<ErrorMessage, SignQuoteResult> = either {
        val result = mutateQuotes(quoteIds).bind()

        val signResponse = result.signOrApproveQuotes.asSignQuoteResponse?.signResponse
        val errorMessage = signResponse?.asFailedToStartSign?.errorMessage
        ensure(signResponse?.asFailedToStartSign == null) { ErrorMessage(errorMessage) }

        when {
            signResponse?.asSimpleSignSession != null -> {
                cacheManager.clearCache()
                SignQuoteResult.StartSimpleSign
            }
            signResponse?.asSwedishBankIdSession != null -> {
                val token = signResponse.asSwedishBankIdSession?.autoStartToken
                ensureNotNull(token) { ErrorMessage(null) }
                SignQuoteResult.StartSwedishBankId(token)
            }
            else -> ErrorMessage("Response unknown").left().bind()
        }
    }

    private suspend fun mutateQuotes(quoteIds: List<String>): Either<ErrorMessage, SignQuotesMutation.Data> {
        return apolloClient.mutate(SignQuotesMutation(quoteIds))
            .safeQuery()
            .toEither()
            .mapLeft { ErrorMessage(it.message) }
    }
}
