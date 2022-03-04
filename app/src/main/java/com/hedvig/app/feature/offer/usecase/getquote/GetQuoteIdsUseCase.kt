package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.nonEmptyListOf
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.LastQuoteIdQuery
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.first

class GetQuoteIdsUseCase(
    private val apolloClient: ApolloClient,
    private val featureManager: FeatureManager,
    private val offerRepository: OfferRepository,
) {

    @JvmInline
    value class QuoteIds(val ids: List<String>)

    suspend operator fun invoke(quoteCartId: String?): Either<ErrorMessage, QuoteIds> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART) && quoteCartId != null) {
            getIdsFromQuoteCart(quoteCartId)
        } else {
            getIdsFromLastQuoteOfMember()
        }
    }

    private suspend fun getIdsFromQuoteCart(quoteCartId: String): Either<ErrorMessage, QuoteIds> {
        return offerRepository.offer(nonEmptyListOf(quoteCartId))
            .first()
            .map {
                val quoteIds = it.quoteBundle.quotes.map { it.id }
                QuoteIds(quoteIds)
            }
    }

    private suspend fun getIdsFromLastQuoteOfMember(): Either<ErrorMessage, QuoteIds> = apolloClient
        .query(LastQuoteIdQuery())
        .safeQuery()
        .toEither { ErrorMessage(it) }
        .map {
            QuoteIds(listOf(it.lastQuoteOfMember.asCompleteQuote!!.id))
        }
}
