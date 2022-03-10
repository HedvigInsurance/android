package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.left
import arrow.core.nonEmptyListOf
import com.apollographql.apollo.ApolloClient
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.first

class GetQuoteIdsUseCase(
    private val apolloClient: ApolloClient,
    private val featureManager: FeatureManager,
    private val offerRepository: OfferRepository,
) {

    data class Error(val message: String?)

    @JvmInline
    value class QuoteIds(val ids: List<String>)

    suspend operator fun invoke(quoteCartId: String?): Either<Error, QuoteIds> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART) && quoteCartId != null) {
            getIdsFromQuoteCart(quoteCartId)
        } else {
            getIdsFromLastQuoteOfMember()
        }
    }

    private suspend fun getIdsFromQuoteCart(quoteCartId: String): Either<Error, QuoteIds> {
        return when (val result = offerRepository.offer(nonEmptyListOf(quoteCartId)).first()) {
            is OfferRepository.OfferResult.Error -> Either.Left(Error(null))
            is OfferRepository.OfferResult.Success -> {
                val ids = QuoteIds(result.data.quoteBundle.quotes.map { it.id })
                Either.Right(ids)
            }
        }
    }

    private fun getIdsFromLastQuoteOfMember(): Either<Error, QuoteIds> {
        return Error(null).left()
    }
}
