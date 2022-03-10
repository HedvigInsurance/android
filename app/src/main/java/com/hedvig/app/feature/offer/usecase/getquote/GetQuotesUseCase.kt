package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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

    operator fun invoke(quoteIds: List<String>, quoteCartId: String?): Flow<Result> {
        return flow {
            when {
                featureManager.isFeatureEnabled(Feature.QUOTE_CART) -> {
                    quoteCartId
                        ?.let { emitAll(getOffer(nonEmptyListOf(quoteCartId))) }
                        ?: emit(Result.Error("No quote cart id found"))
                }
                quoteIds.isNotEmpty() -> emitAll(getOffer(NonEmptyList.fromListUnsafe(quoteIds)))
                else -> {
                    when (val result = getQuoteIdsUseCase.invoke(null)) {
                        is Either.Left -> emit(Result.Error(result.value.message))
                        is Either.Right -> {
                            if (result.value.ids.isEmpty()) {
                                emit(Result.Error(null))
                            } else {
                                emitAll(getOffer(NonEmptyList.fromListUnsafe(result.value.ids)))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getOffer(id: NonEmptyList<String>): Flow<Result> = offerRepository.offer(id).map { offerResult ->
        when (offerResult) {
            is OfferRepository.OfferResult.Error -> Result.Error(offerResult.message)
            is OfferRepository.OfferResult.Success -> Result.Success(offerResult.data)
        }
    }
}
