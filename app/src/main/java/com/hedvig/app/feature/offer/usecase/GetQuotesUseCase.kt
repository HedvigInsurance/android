package com.hedvig.app.feature.offer.usecase

import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import kotlinx.coroutines.flow.Flow

class GetQuotesUseCase(
    private val offerRepository: OfferRepository,
) {
    sealed class Result {
        data class Success(
            val ids: List<String>,
            val data: Flow<OfferRepository.OfferResult>,
        ) : Result()

        object Error : Result()
    }

    suspend operator fun invoke(ids: List<String>): Result {
        if (ids.isEmpty()) {
            val idResult = offerRepository.quoteIdOfLastQuoteOfMember().safeQuery()
            if (idResult !is QueryResult.Success) {
                return Result.Error
            }
            val id = idResult.data.lastQuoteOfMember.asCompleteQuote?.id?.let { listOf(it) } ?: return Result.Error
            return Result.Success(id, offerRepository.offer(id))
        } else {
            return Result.Success(ids, offerRepository.offer(ids))
        }
    }
}
