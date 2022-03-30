package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import com.hedvig.app.util.ErrorMessage
import kotlinx.coroutines.flow.firstOrNull

class GetPostSignDependenciesUseCase(
    private val offerRepository: OfferRepository,
) {
    data class Result(
        val postSignScreen: PostSignScreen,
        val displayName: String,
    )

    suspend operator fun invoke(quoteIds: List<String>): Either<ErrorMessage, Result> {
        return either {
            val ids = NonEmptyList.fromList(quoteIds).toEither { ErrorMessage() }.bind()
            val result = offerRepository
                .offer(ids)
                .firstOrNull()
                ?.bind()?.toResult()
            ensureNotNull(result) { ErrorMessage() }
        }
    }

    private fun OfferModel.toResult() = Result(
        quoteBundle.viewConfiguration.postSignScreen,
        quoteBundle.name
    )
}
