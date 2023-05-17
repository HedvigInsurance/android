package com.hedvig.app.feature.offer.usecase

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.apollo.OperationResult
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class FakeObserveQuoteCartCheckoutUseCase : ObserveQuoteCartCheckoutUseCase {

  val results = Turbine<Either<OperationResult.Error, Checkout>>()

  override fun invoke(quoteCartId: QuoteCartId): Flow<Either<OperationResult.Error, Checkout>> {
    return results.asChannel().receiveAsFlow()
  }
}
