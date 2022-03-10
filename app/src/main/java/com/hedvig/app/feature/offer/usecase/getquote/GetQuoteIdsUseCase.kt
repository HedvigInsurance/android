package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.nonEmptyListOf
import com.hedvig.app.feature.offer.OfferRepository
import kotlinx.coroutines.flow.first

class GetQuoteIdsUseCase(
    private val offerRepository: OfferRepository,
) {

    data class Error(val message: String?)

    @JvmInline
    value class QuoteIds(val ids: List<String>)

    suspend operator fun invoke(quoteCartId: String): Either<Error, QuoteIds> {
        return when (val result = offerRepository.offer(nonEmptyListOf(quoteCartId)).first()) {
            is OfferRepository.OfferResult.Error -> Either.Left(Error(null))
            is OfferRepository.OfferResult.Success -> {
                val ids = QuoteIds(result.data.quoteBundle.quotes.map { it.id })
                Either.Right(ids)
            }
        }
    }
}
