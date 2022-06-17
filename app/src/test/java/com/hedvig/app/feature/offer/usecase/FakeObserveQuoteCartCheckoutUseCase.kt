package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.apollo.QueryResult
import kotlinx.coroutines.flow.Flow

class FakeObserveQuoteCartCheckoutUseCase(
    private val function: (QuoteCartId) -> Flow<Either<QueryResult.Error, Checkout>>,
) : ObserveQuoteCartCheckoutUseCase {
    override fun invoke(quoteCartId: QuoteCartId): Flow<Either<QueryResult.Error, Checkout>> {
        return function(quoteCartId)
    }
}
