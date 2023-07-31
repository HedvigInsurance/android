package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.hanalytics.HAnalytics
import giraffe.QuoteCartStartCheckoutMutation

class StartCheckoutUseCase(
  private val apolloClient: ApolloClient,
  private val cacheManager: NetworkCacheManager,
  private val hAnalytics: HAnalytics,
) {
  object Success

  suspend fun startCheckoutAndClearCache(
    quoteCartId: QuoteCartId?,
    quoteIds: List<String>,
  ): Either<ErrorMessage, Success> = either {
    ensureNotNull(quoteCartId) { ErrorMessage("Quote cart id not found") }

    val result = mutateQuoteCart(quoteCartId, quoteIds).bind()
    val errorMessage = result.quoteCartStartCheckout.asBasicError?.message

    ensure(errorMessage == null) { ErrorMessage(errorMessage) }
    cacheManager.clearCache()
    hAnalytics.quotesSigned(quoteIds)
    Success
  }

  private suspend fun mutateQuoteCart(
    quoteCartId: QuoteCartId,
    quoteIds: List<String>,
  ): Either<ErrorMessage, QuoteCartStartCheckoutMutation.Data> = apolloClient
    .mutation(QuoteCartStartCheckoutMutation(quoteCartId.id, quoteIds))
    .safeExecute()
    .toEither(::ErrorMessage)
}
