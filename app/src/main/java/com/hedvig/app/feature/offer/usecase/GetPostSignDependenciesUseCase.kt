package com.hedvig.app.feature.offer.usecase

import arrow.core.NonEmptyList
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import kotlinx.coroutines.flow.first

class GetPostSignDependenciesUseCase(
    private val offerRepository: OfferRepository,
) {
    sealed class Result {
        data class Success(
            val postSignScreen: PostSignScreen,
            val displayName: String,
        ) : Result()

        object Error : Result()
    }

    suspend operator fun invoke(quoteIds: List<String>): Result {
        val offer = runCatching {
            offerRepository
                .offer(NonEmptyList.fromListUnsafe(quoteIds))
                .first()
        }.getOrNull() ?: return Result.Error
        if (offer !is OfferRepository.OfferResult.Success) {
            return Result.Error
        }

        return Result.Success(
            offer.data.quoteBundle.viewConfiguration.postSignScreen,
            offer.data.quoteBundle.name,
        )
    }
}
