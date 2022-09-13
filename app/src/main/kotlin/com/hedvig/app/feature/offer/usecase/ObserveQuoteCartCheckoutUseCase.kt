package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.apollo.OperationResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

interface ObserveQuoteCartCheckoutUseCase {
  fun invoke(quoteCartId: QuoteCartId): Flow<Either<OperationResult.Error, Checkout>>
}

class ObserveQuoteCartCheckoutUseCaseImpl(
  private val getQuoteCartCheckoutUseCase: GetQuoteCartCheckoutUseCase,
) : ObserveQuoteCartCheckoutUseCase {
  override fun invoke(quoteCartId: QuoteCartId): Flow<Either<OperationResult.Error, Checkout>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val result = either {
          val checkout = getQuoteCartCheckoutUseCase.invoke(quoteCartId).bind()
          ensureNotNull(checkout) {
            OperationResult.Error.NoDataError("Checkout was null")
          }
          checkout
        }
        emit(result)
        delay(fetchFrequency)
      }
    }
  }

  companion object {
    private val fetchFrequency = 500.milliseconds
  }
}
