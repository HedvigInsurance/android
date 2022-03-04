package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.util.ErrorMessage
import kotlinx.coroutines.flow.firstOrNull

class GetQuoteUseCase(
    private val offerRepository: OfferRepository,
) {

    suspend operator fun invoke(bundleIds: List<String>, quoteId: String): Either<ErrorMessage, QuoteBundle.Quote> {
        if (bundleIds.isEmpty()) {
            return ErrorMessage("No bundle ids found").left()
        }

        return offerRepository
            .offer(NonEmptyList.fromListUnsafe(bundleIds))
            .firstOrNull()
            ?.flatMap {
                val quote = it.quoteBundle.quotes.firstOrNull { it.id == quoteId }
                quote?.right() ?: ErrorMessage("No quote found with id $quoteId").left()
            } ?: ErrorMessage("No quote found").left()
    }
}
