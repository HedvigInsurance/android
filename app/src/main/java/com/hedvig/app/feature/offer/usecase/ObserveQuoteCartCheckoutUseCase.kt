package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.QuoteCartCheckoutStatusQuery
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.toCheckout
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

interface ObserveQuoteCartCheckoutUseCase {
    fun invoke(quoteCartId: QuoteCartId): Flow<Either<QueryResult.Error, Checkout>>
}

class ObserveQuoteCartCheckoutUseCaseImpl(
    private val apolloClient: ApolloClient,
) : ObserveQuoteCartCheckoutUseCase {
    override fun invoke(quoteCartId: QuoteCartId): Flow<Either<QueryResult.Error, Checkout>> {
        return flow {
            while (currentCoroutineContext().isActive) {
                val result = either<QueryResult.Error, Checkout> {
                    val checkout = apolloClient.query(QuoteCartCheckoutStatusQuery(quoteCartId.id))
                        .toBuilder()
                        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
                        .build()
                        .safeQuery()
                        .toEither()
                        .bind()
                        .quoteCart
                        .checkout
                    ensureNotNull(checkout) {
                        QueryResult.Error.NoDataError(null)
                    }
                    checkout.toCheckout()
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
