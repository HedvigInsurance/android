package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.computations.either
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.toNel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GetQuotesUseCase(
    private val offerRepository: OfferRepository,
    private val featureManager: FeatureManager,
    private val getQuoteIdsUseCase: GetQuoteIdsUseCase,
) {
    data class Success(val data: OfferModel)
    data class Error(val message: String?)

    operator fun invoke(quoteIds: List<String>, quoteCartId: String?): Flow<Either<Error, Success>> {
        return flow {
            when {
                featureManager.isFeatureEnabled(Feature.QUOTE_CART) -> {
                    quoteCartId
                        ?.let { emitAll(getOffer(nonEmptyListOf(quoteCartId))) }
                        ?: emit(Error("No quote cart id found").left())
                }
                quoteIds.isNotEmpty() -> emitAll(getOffer(NonEmptyList.fromListUnsafe(quoteIds)))
                else -> {
                    val result = either<Error, NonEmptyList<String>> {
                        val quoteIdsResult = getQuoteIdsUseCase.invoke(null)
                            .mapLeft { Error(null) }
                            .bind()
                        quoteIdsResult.ids.toNel { Error(null) }.bind()
                    }
                    when (result) {
                        is Either.Left -> emit(result.value.left())
                        is Either.Right -> emitAll(getOffer(result.value))
                    }
                }
            }
        }
    }

    private fun getOffer(id: NonEmptyList<String>): Flow<Either<Error, Success>> = offerRepository
        .offer(id)
        .map { offerResult ->
            when (offerResult) {
                is OfferRepository.OfferResult.Error -> Error(offerResult.message).left()
                is OfferRepository.OfferResult.Success -> Success(offerResult.data).right()
            }
        }
}
