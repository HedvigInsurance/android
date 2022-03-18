package com.hedvig.app.feature.checkout

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.safeQuery
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ApproveQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val offerRepository: OfferRepository,
    private val cacheManager: CacheManager,
) {

    data class ApproveSuccess(
        val date: LocalDate?,
        val postSignScreen: PostSignScreen,
        val bundleName: String
    )

    sealed class Error {
        data class GeneralError(val message: String? = null) : Error()
        data class ApproveError(val postSignScreen: PostSignScreen) : Error()
    }

    suspend fun approveQuotesAndClearCache(quoteIds: List<String>): Either<Error, ApproveSuccess> = either {
        val offerModel = offerRepository.offerFlow(quoteIds)
            .first()
            .mapLeft { Error.GeneralError("Could not get Quote") }
            .bind()

        val date = signQuotes(quoteIds, offerModel.quoteBundle.viewConfiguration.postSignScreen).bind()

        ApproveSuccess(
            date = date,
            postSignScreen = offerModel.quoteBundle.viewConfiguration.postSignScreen,
            bundleName = offerModel.quoteBundle.name
        )
    }

    private suspend fun signQuotes(
        quoteIds: List<String>,
        postSignScreen: PostSignScreen
    ): Either<Error, LocalDate?> = apolloClient
        .mutate(SignQuotesMutation(quoteIds))
        .safeQuery()
        .toEither { Error.GeneralError(it) }
        .flatMap {
            it.signOrApproveQuotes.asApproveQuoteResponse?.approved?.let { approved ->
                if (approved) {
                    val startDate = readCachedStartDate(quoteIds)
                    cacheManager.clearCache()
                    startDate.right()
                } else {
                    Error.ApproveError(postSignScreen).left()
                }
            } ?: Error.GeneralError().left()
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
