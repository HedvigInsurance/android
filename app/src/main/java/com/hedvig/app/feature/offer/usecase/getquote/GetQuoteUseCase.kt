package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.util.ErrorMessage
import kotlinx.coroutines.flow.firstOrNull

class GetQuoteUseCase(
    private val offerRepository: OfferRepository,
) {

    suspend operator fun invoke(bundleIds: List<String>, quoteId: String): Either<ErrorMessage, QuoteBundle.Quote> {
        return either {
            ensure(bundleIds.isNotEmpty()) { ErrorMessage("No bundle ids found") }

            val offerModel = offerRepository.offer(NonEmptyList.fromListUnsafe(bundleIds)).firstOrNull()?.bind()
            val quote = offerModel?.quoteBundle?.quotes?.firstOrNull { it.id == quoteId }

            ensureNotNull(quote) { ErrorMessage("No quote found with id $quoteId") }
        }
    }
}
