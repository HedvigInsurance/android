package com.hedvig.app.feature.offer.usecase

import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class RefreshQuotesUseCase(
    private val offerRepository: OfferRepository
) {
    sealed class Result {
        object Success : Result()
        object Error : Result()
    }

    suspend operator fun invoke(ids: List<String>) = when (offerRepository.refreshOfferQuery(ids).safeQuery()) {
        is QueryResult.Success -> Result.Success
        else -> Result.Error
    }
}
