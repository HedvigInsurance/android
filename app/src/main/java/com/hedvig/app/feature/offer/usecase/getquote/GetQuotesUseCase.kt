package com.hedvig.app.feature.offer.usecase.getquote

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class GetQuotesUseCase(
    private val offerRepository: OfferRepository,
    private val featureManager: FeatureManager,
) {

    operator fun invoke(quoteIds: List<String>, quoteCartId: String?): Flow<Either<ErrorMessage, OfferModel>> {
        return flow {
            when {
                featureManager.isFeatureEnabled(Feature.QUOTE_CART) -> {
                    quoteCartId
                        ?.let { emitAll(offerRepository.offer(nonEmptyListOf(quoteCartId))) }
                        ?: emit(ErrorMessage("No quote cart id found").left())
                }
                quoteIds.isNotEmpty() -> emitAll(offerRepository.offer((NonEmptyList.fromListUnsafe(quoteIds))))
                else -> ErrorMessage(null)
            }
        }
    }
}
