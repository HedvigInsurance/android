package com.hedvig.app.feature.offer.ui.checkout

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import java.time.LocalDate

class ApproveQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val offerRepository: OfferRepository,
    private val tracker: OfferTracker,
    private val cacheManager: CacheManager
) {

    sealed class ApproveQuotesResult {
        data class Success(val date: LocalDate?) : ApproveQuotesResult()
        sealed class Error : ApproveQuotesResult() {
            data class GeneralError(val message: String? = null) : Error()
            object ApproveError : Error()
        }
    }

    suspend fun approveQuotesAndClearCache(quoteIds: List<String>): ApproveQuotesResult {
        val mutation = SignQuotesMutation(quoteIds)
        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Error -> ApproveQuotesResult.Error.GeneralError(result.message)
            is QueryResult.Success -> {
                val approveResponseResponse = result.data?.signOrApproveQuotes?.asApproveQuoteResponse
                approveResponseResponse?.approved?.let { approved ->
                    if (approved) {
                        val startDate = readCachedStartDate(quoteIds)
                        tracker.signQuotes(quoteIds)
                        cacheManager.clearCache()
                        ApproveQuotesResult.Success(startDate)
                    } else {
                        ApproveQuotesResult.Error.ApproveError
                    }
                } ?: ApproveQuotesResult.Error.GeneralError()
            }
        }
    }

    private fun readCachedStartDate(quoteIds: List<String>): LocalDate? {
        val cachedData = apolloClient
            .apolloStore
            .read(offerRepository.offerQuery(quoteIds))
            .execute()

        return cachedData.quoteBundle.inception.asConcurrentInception?.startDate
            ?: cachedData.quoteBundle.inception.asIndependentInceptions?.inceptions?.firstOrNull()?.startDate
    }
}
