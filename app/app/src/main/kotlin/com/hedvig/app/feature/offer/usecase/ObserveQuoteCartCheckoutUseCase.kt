package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.app.feature.offer.model.Checkout
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
