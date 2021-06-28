package com.hedvig.app.feature.offer.usecase

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.OfferRepository
import kotlinx.coroutines.flow.first

class GetQuoteUseCase(
    private val offerRepository: OfferRepository,
) {
    sealed class Result {
        data class Success(
            val quote: OfferQuery.Quote,
        ) : Result()

        object Error : Result()
    }

    suspend operator fun invoke(bundleIds: List<String>, quoteId: String): Result {
        val offer = offerRepository
            .offer(bundleIds)
            .first()

        if (offer.hasErrors()) {
            return Result.Error
        }

        val data = offer.data ?: return Result.Error

        val quote = data.quoteBundle.quotes.firstOrNull { it.id == quoteId } ?: return Result.Error

        return Result.Success(quote)
    }
}
