package com.hedvig.app.feature.offer.usecase

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.OfferQuery
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
            val data: Flow<Response<OfferQuery.Data>>,
        ) : Result()

        data class Error(val message: String?) : Result()
    }

    suspend operator fun invoke(ids: List<String>): Result {
        if (ids.isEmpty()) {
            when (val idResult = offerRepository.quoteIdOfLastQuoteOfMember().safeQuery()) {
                is QueryResult.Error -> return Result.Error(idResult.message)
                is QueryResult.Success -> {
                    val id = idResult.data
                        .lastQuoteOfMember
                        .asCompleteQuote
                        ?.id
                        ?.let { listOf(it) }
                        ?: return Result.Error("")
                    return Result.Success(id, offerRepository.offer(id))
                }
            }
        } else {
            return Result.Success(ids, offerRepository.offer(ids))
        }
    }
}
