package com.hedvig.app.feature.offer.usecase

import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferViewModel
import kotlinx.coroutines.flow.first

class GetPostSignDependenciesUseCase(
    private val offerRepository: OfferRepository,
) {
    sealed class Result {
        data class Success(
            val postSignScreen: OfferViewModel.PostSignScreen,
            val displayName: String,
        ) : Result()

        object Error : Result()
    }

    suspend operator fun invoke(quoteIds: List<String>): Result {
        val offer = runCatching {
            offerRepository
                .offer(quoteIds)
                .first()
        }.getOrNull() ?: return Result.Error
        if (offer !is OfferRepository.OfferResult.Success) {
            return Result.Error
        }

        return Result.Success(
            OfferViewModel.PostSignScreen.from(offer.data.quoteBundle.appConfiguration.postSignStep),
            offer.data.quoteBundle.displayName,
        )
    }
}
