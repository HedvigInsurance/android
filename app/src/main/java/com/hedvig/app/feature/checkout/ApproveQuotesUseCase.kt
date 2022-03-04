package com.hedvig.app.feature.checkout

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.safeQuery
import java.time.LocalDate

class ApproveQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val offerRepository: OfferRepository,
    private val cacheManager: CacheManager
) {

    sealed class Error {
        data class GeneralError(val message: String? = null) : Error()
        object ApproveError : Error()
    }

    suspend fun approveQuotesAndClearCache(quoteIds: List<String>): Either<Error, LocalDate?> {
        return apolloClient.mutate(SignQuotesMutation(quoteIds))
            .safeQuery()
            .toEither { Error.GeneralError(it) }
            .flatMap {
                it.signOrApproveQuotes.asApproveQuoteResponse?.approved?.let { approved ->
                    if (approved) {
                        val startDate = readCachedStartDate(quoteIds)
                        cacheManager.clearCache()
                        startDate.right()
                    } else {
                        Error.ApproveError.left()
                    }
                } ?: Error.GeneralError().left()
            }
    }

    private fun readCachedStartDate(quoteIds: List<String>): LocalDate? {
        val cachedData = apolloClient
            .apolloStore
            .read(offerRepository.offerQuery(quoteIds))
            .execute()

        return cachedData.quoteBundle.fragments.quoteBundleFragment.inception.asConcurrentInception?.startDate
            ?: cachedData.quoteBundle.fragments.quoteBundleFragment.inception.asIndependentInceptions?.inceptions
                ?.firstOrNull()?.startDate
    }
}
