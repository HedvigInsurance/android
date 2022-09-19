package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import com.hedvig.android.apollo.OperationResult
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import kotlinx.coroutines.flow.Flow

class FakeObserveQuoteCartCheckoutUseCase(
  private val function: (QuoteCartId) -> Flow<Either<OperationResult.Error, Checkout>>,
) : ObserveQuoteCartCheckoutUseCase {
  override fun invoke(quoteCartId: QuoteCartId): Flow<Either<OperationResult.Error, Checkout>> {
    return function(quoteCartId)
  }
}
