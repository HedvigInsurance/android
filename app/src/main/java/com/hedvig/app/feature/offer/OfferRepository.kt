package com.hedvig.app.feature.offer

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.owldroid.graphql.QuoteCartQuery
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteCartFragmentToOfferModelMapper
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toEither
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class OfferRepository(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
  private val quoteCartFragmentToOfferModelMapper: QuoteCartFragmentToOfferModelMapper,
  private val hAnalytics: HAnalytics,
) {

  val offerFlow: MutableSharedFlow<Either<ErrorMessage, OfferModel>> = MutableSharedFlow(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
  )

  suspend fun queryAndEmitOffer(quoteCartId: QuoteCartId) {
    val offer = queryQuoteCart(quoteCartId)
    offerFlow.tryEmit(offer)
  }

  private suspend fun queryQuoteCart(
    id: QuoteCartId,
  ): Either<ErrorMessage, OfferModel> = either {
    val result = apolloClient
      .query(QuoteCartQuery(localeManager.defaultLocale(), id.id))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeQuery()
      .toEither { ErrorMessage(it) }
      .bind()

    val quoteCartFragment = result.quoteCart.fragments.quoteCartFragment
    val bundle = quoteCartFragment.bundle
    ensureNotNull(bundle) {
      ErrorMessage("No quotes in offer, please try again")
    }
    val receivedQuoteIds = bundle.possibleVariations
      .flatMap { variation -> variation.bundle.fragments.quoteBundleFragment.quotes }
      .map { quote -> quote.id }
    hAnalytics.receivedQuotes(receivedQuoteIds)

    quoteCartFragmentToOfferModelMapper.map(quoteCartFragment)
  }
}
