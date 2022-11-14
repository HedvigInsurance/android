package com.hedvig.app.feature.offer.usecase

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage

class FakeCreateAccessTokenUseCase : CreateAccessTokenUseCase {

  val results = Turbine<Either<ErrorMessage, CreateAccessTokenUseCase.Success>>()

  override suspend fun invoke(quoteCartId: QuoteCartId): Either<ErrorMessage, CreateAccessTokenUseCase.Success> {
    return results.awaitItem()
  }
}
