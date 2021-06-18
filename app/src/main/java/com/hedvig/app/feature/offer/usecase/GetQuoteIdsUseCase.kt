package com.hedvig.app.feature.offer.usecase

import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetQuoteIdsUseCase(
    private val offerRepository: OfferRepository,
) {
    sealed class Result {
        data class Success(
            val ids: List<String>,
        ) : Result()

        object Error : Result()
    }

    suspend operator fun invoke(ids: List<String>): Result {
        if (ids.isEmpty()) {
            val idResult = offerRepository.quoteIdOfLastQuoteOfMember().safeQuery()
            if (idResult !is QueryResult.Success) {
                return Result.Error
            }
            val id = idResult.data.lastQuoteOfMember.asCompleteQuote?.id ?: return Result.Error
            return Result.Success(listOf(id))
        } else {
            return Result.Success(ids)
        }
    }
}
