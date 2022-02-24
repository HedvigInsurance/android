package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetQuotesUseCase(
    private val offerRepository: OfferRepository,
    private val featureManager: FeatureManager,
    private val getQuoteIdsUseCase: GetQuoteIdsUseCase,
) {
    sealed class Result {
        data class Success(val data: OfferModel) : Result()
        data class Error(val message: String?) : Result()
    }

    suspend operator fun invoke(quoteIds: List<String>, quoteCartId: String?): Flow<Result> {
        return when {
            featureManager.isFeatureEnabled(Feature.QUOTE_CART) -> {
                getOffer(listOf(quoteCartId!!))
            }
            quoteIds.isNotEmpty() -> {
                getOffer(quoteIds)
            }
            else -> {
                when (val result = getQuoteIdsUseCase.invoke(null)) {
                    is Either.Left -> flowOf(Result.Error(result.value.message))
                    is Either.Right -> getOffer(result.value.ids)
                }
            }
        }
    }

    private fun getOffer(id: List<String>): Flow<Result> {
        return offerRepository.offer(id).map { offerResult ->
            when (offerResult) {
                is OfferRepository.OfferResult.Error -> Result.Error(offerResult.message)
                is OfferRepository.OfferResult.Success -> Result.Success(offerResult.data)
            }
        }
    }
}
