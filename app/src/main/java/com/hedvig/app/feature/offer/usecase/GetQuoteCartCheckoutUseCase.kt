package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
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
                .toBuilder()
                .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
                .build()
                .safeQuery()
                .toEither()
                .bind()
                .quoteCart
                .checkout

            checkout?.toCheckout()
        }
    }
}
