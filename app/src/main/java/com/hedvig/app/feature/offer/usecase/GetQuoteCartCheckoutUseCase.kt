package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.owldroid.graphql.QuoteCartCheckoutStatusQuery
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.toCheckout
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetQuoteCartCheckoutUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(quoteCartId: QuoteCartId): Either<QueryResult.Error, Checkout?> {
    return either {
      val checkout = apolloClient.query(QuoteCartCheckoutStatusQuery(quoteCartId.id))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeQuery()
        .toEither()
        .bind()
        .quoteCart
        .checkout

      checkout?.toCheckout()
    }
  }
}
