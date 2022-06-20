package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage

class FakeCreateAccessTokenUseCase(
    private val function: suspend (QuoteCartId) -> Either<ErrorMessage, CreateAccessTokenUseCase.Success>,
) : CreateAccessTokenUseCase {
    override suspend fun invoke(quoteCartId: QuoteCartId): Either<ErrorMessage, CreateAccessTokenUseCase.Success> {
        return function(quoteCartId)
    }
}
